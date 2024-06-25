package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.compiletime.ops.int._
import scala.deriving.Mirror
import scala.quoted._

import pureconfig.error.{ConfigReaderFailures, KeyNotFound, WrongSizeList}
import pureconfig.generic.ProductHint.UseOrDefault
import pureconfig.generic.derivation.Utils
import pureconfig.generic.derivation.Utils.widen

import ProductDerivationMacros._

trait HintsAwareProductConfigReaderDerivation { self: HintsAwareConfigReaderDerivation =>
  inline def deriveProductReader[A](using pm: Mirror.ProductOf[A], ph: ProductHint[A]): ConfigReader[A] =
    inline erasedValue[A] match {
      case _: Tuple =>
        new ConfigReader[A] {
          def from(cur: ConfigCursor): ConfigReader.Result[A] =
            for {
              listCur <- asList(cur)
              result <- readTuple[A & Tuple, 0](listCur.list.toVector)
            } yield result

          def asList(cur: ConfigCursor) =
            cur.asListCursor.flatMap { listCur =>
              if (constValue[Tuple.Size[A & Tuple]] == listCur.size)
                Right(listCur)
              else
                listCur.failed(
                  WrongSizeList(constValue[Tuple.Size[A & Tuple]], listCur.size)
                )
            }
        }

      case _ =>
        new ConfigReader[A] {
          def from(cur: ConfigCursor): ConfigReader.Result[A] = {
            val tupleSize = summonInline[ValueOf[Tuple.Size[pm.MirroredElemTypes]]]
            val defaults = getDefaults[A](tupleSize.value)

            for {
              objCursor <- cur.asObjectCursor
              labels = Utils.transformedLabels(identity).toVector
              actions = labels.map { label => label -> ph.from(objCursor, label) }.toMap
              result <- readCaseClass[pm.MirroredElemTypes, 0, A](objCursor, labels, actions, defaults)
            } yield pm.fromProduct(result)
          }
        }
    }

  private inline def readCaseClass[T <: Tuple, N <: Int, A: ProductHint](
      objCursor: ConfigObjectCursor,
      labels: Vector[String],
      actions: Map[String, ProductHint.Action],
      defaults: Vector[DefaultValue]
  ): Either[ConfigReaderFailures, T] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val n = constValue[N]
        lazy val reader = summonConfigReader[h]
        val default = defaults(n)
        val label = labels(n)
        val fieldHint = actions(label)

        val head =
          (fieldHint, default) match {
            case (UseOrDefault(cursor, _), Some(defaultValue)) if cursor.isUndefined =>
              Right(defaultValue().asInstanceOf[h])
            case (action, _) if reader.isInstanceOf[ReadsMissingKeys] || !action.cursor.isUndefined =>
              reader.from(action.cursor)
            case _ =>
              objCursor.failed(KeyNotFound.forKeys(fieldHint.field, objCursor.keys))
          }

        val tail = readCaseClass[t, N + 1, A](objCursor, labels, actions, defaults)

        val resultTuple = ConfigReader.Result.zipWith(head, tail)((h, t) => widen[h *: t, T](h *: t))

        val usedFields = actions.map(_._2.field).toSet
        val hintFailures = summon[ProductHint[A]].bottom(objCursor, usedFields).toLeft(())

        ConfigReader.Result.zipWith(resultTuple, hintFailures)((r, _) => r)

      case _: EmptyTuple =>
        Right(widen[EmptyTuple, T](EmptyTuple))
    }

  private inline def readTuple[T <: Tuple, N <: Int](cursors: Vector[ConfigCursor]): Either[ConfigReaderFailures, T] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val n = constValue[N]
        val reader = summonConfigReader[h]
        val cursor = cursors(n)

        val head = reader.from(cursor)
        val tail = readTuple[t, N + 1](cursors)

        ConfigReader.Result.zipWith(head, tail)((h, t) => widen[h *: t, T](h *: t))

      case _: EmptyTuple =>
        Right(widen[EmptyTuple, T](EmptyTuple))
    }

}

private[scala3] object ProductDerivationMacros {
  type DefaultValue = Option[() => Any]

  inline def getDefaults[T](inline size: Int): Vector[DefaultValue] = ${ getDefaultsImpl[T]('size) }

  private def getDefaultsImpl[T](size: Expr[Int])(using Quotes, Type[T]): Expr[Vector[DefaultValue]] = {
    import quotes.reflect._

    val n = size.valueOrAbort
    val typeRepr = TypeRepr.of[T]

    def defaultMethodAt(i: Int) =
      typeRepr.typeSymbol.companionClass.declaredMethod(s"$$lessinit$$greater$$default$$$i").headOption
    def callMethod(symbol: Symbol) =
      Ref(typeRepr.typeSymbol.companionModule).select(symbol).appliedToTypes(typeRepr.typeArgs)

    val expr = Expr.ofSeq {
      (1 to n).map { i =>
        defaultMethodAt(i) match {
          case Some(value) => '{ Some(() => ${ callMethod(value).asExpr }) }
          case None => Expr(None)
        }
      }
    }

    '{ $expr.toVector }
  }
}
