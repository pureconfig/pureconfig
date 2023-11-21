package pureconfig
package generic
package derivation

import scala.compiletime.ops.int.*
import scala.compiletime.{constValue, constValueTuple, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror
import scala.quoted.*
import scala.util.chaining.*

import pureconfig.error.{ConfigReaderFailures, ConvertFailure, KeyNotFound, UnknownKey, WrongSizeList}
import pureconfig.generic.ProductHint.UseOrDefault
import pureconfig.generic.derivation.WidenType.widen

trait ProductConfigReaderDerivation(fieldMapping: ConfigFieldMapping) { self: ConfigReaderDerivation =>
  inline def derivedProduct[A](using m: Mirror.ProductOf[A], hint: ProductHint[A]): ConfigReader[A] =
    val tupleSize = summonInline[ValueOf[Tuple.Size[m.MirroredElemTypes]]]
    val defaults = ProductDerivationMacros.getDefaults[A](tupleSize.value)

    inline erasedValue[A] match {
      case _: Tuple =>
        new ConfigReader[A] {
          def from(cur: ConfigCursor): ConfigReader.Result[A] =
            for {
              listCur <- asList(cur)
              result <- readTuple[A & Tuple, 0](listCur.list)
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
          def from(cur: ConfigCursor): ConfigReader.Result[A] =
            for {
              objCursor <- cur.asObjectCursor
              labels = Labels.transformed[m.MirroredElemLabels](fieldMapping)
              result <- readCaseClass[m.MirroredElemTypes, 0, A](
                objCursor,
                labels,
                labels.map(objCursor.atKeyOrUndefined(_)),
                defaults,
                hint
              )
            } yield m.fromProduct(result)

        }
    }

  inline def readCaseClass[T <: Tuple, N <: Int, A](
      objCursor: ConfigObjectCursor,
      labels: List[String],
      cursors: List[ConfigCursor],
      defaults: Vector[Option[Any]],
      hint: ProductHint[A]
  ): Either[ConfigReaderFailures, T] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val n = constValue[N]
        val cursor = cursors(n)
        val reader = summonConfigReader[h]
        val default = defaults(n)
        val label = labels(n)
        val fieldHint = hint.from(objCursor, label)

        val h =
          (fieldHint, default) match {
            case (UseOrDefault(cursor, _), Some(defaultValue)) if cursor.isUndefined =>
              Right(defaultValue.asInstanceOf[h])
            case (action, _) if reader.isInstanceOf[ReadsMissingKeys] || !action.cursor.isUndefined =>
              reader.from(action.cursor)
            case _ =>
              cursor.failed(KeyNotFound.forKeys(fieldHint.field, objCursor.keys))
          }

        h -> readCaseClass[t, N + 1, A](objCursor, labels, cursors, defaults, hint) match {
          case (Right(h), Right(t)) => Right(widen[h *: t, T](h *: t))
          case (Left(h), Left(t)) => Left(h ++ t)
          case (_, Left(failures)) => Left(failures)
          case (Left(failures), _) => Left(failures)
        }

      case _: EmptyTuple =>
        Right(widen[EmptyTuple, T](EmptyTuple))
    }

  inline def readTuple[T <: Tuple, N <: Int](
      cursors: List[ConfigCursor],
  ): Either[ConfigReaderFailures, T] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val n = constValue[N]
        val reader = summonConfigReader[h]
        val cursor = cursors(n)

        val h = reader.from(cursor)

        h -> readTuple[t, N + 1](cursors) match {
          case (Right(h), Right(t)) => Right(widen[h *: t, T](h *: t))
          case (Left(h), Left(t)) => Left(h ++ t)
          case (_, Left(failures)) => Left(failures)
          case (Left(failures), _) => Left(failures)
        }

      case _: EmptyTuple =>
        Right(widen[EmptyTuple, T](EmptyTuple))
    }

  inline def summonConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A] => reader
      case given Mirror.Of[A] => ConfigReader.derived[A]
    }
}

object ProductDerivationMacros {
  inline def getDefaults[T](inline size: Int): Vector[Option[Any]] = ${ getDefaultsImpl[T]('size) }

  def getDefaultsImpl[T](size: Expr[Int])(using Quotes, Type[T]): Expr[Vector[Option[Any]]] = {
    import quotes.reflect.*

    val n = size.valueOrError
    val typeRepr = TypeRepr.of[T]

    def defaultMethodAt(i: Int) =
      typeRepr.typeSymbol.companionClass.declaredMethod(s"$$lessinit$$greater$$default$$$i").headOption
    def callMethod(symbol: Symbol) =
      Ref(typeRepr.typeSymbol.companionModule).select(symbol).appliedToTypes(typeRepr.typeArgs)

    val expr = Expr.ofSeq {
      (1 to n).map { i =>
        defaultMethodAt(i) match
          case Some(value) => '{ Some(${ callMethod(value).asExpr }) }
          case None => Expr(None)
      }
    }

    '{ $expr.toVector }
  }
}
