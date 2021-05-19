package pureconfig
package generic
package derivation

import scala.compiletime.{constValue, constValueTuple, erasedValue, summonFrom, summonInline}
import scala.compiletime.ops.int._
import scala.deriving.Mirror

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure, KeyNotFound, UnknownKey, WrongSizeList}
import pureconfig.generic.derivation.WidenType.widen

trait ProductConfigReaderDerivation(hint: ProductHint[?]) { self: ConfigReaderDerivation =>
  trait ProductConfigReader[A] extends DerivedConfigReader[A]
  object ProductConfigReader {
    inline def derived[A](using m: Mirror.ProductOf[A]): ProductConfigReader[A] =
      new ProductConfigReader[A] {
        def from(cur: ConfigCursor): ConfigReader.Result[A] =
          for {
            objCur <- cur.asObjectCursor
            result <- {
              val results =
                for {
                  (field, reader) <- summonFor[A]
                } yield {
                  val action = hint.from(objCur, field)
                  if reader.isInstanceOf[ReadsMissingKeys] || !action.cursor.isUndefined then reader.from(action.cursor)
                  else cur.failed(KeyNotFound.forKeys(action.field, objCur.keys))
                }

              ConfigReader.Result
                .sequence(results)
                .map(
                  _.reverse
                    .foldLeft[Tuple](EmptyTuple)((t, v) => v *: t)
                )
            }
          } yield m.fromProduct(result)
      }

    inline def summonFor[A](using m: Mirror.ProductOf[A]): List[(String, ConfigReader[_])] =
      Labels.of[m.MirroredElemLabels].zip(summonConfigReaders[m.MirroredElemTypes])

    inline def summonConfigReaders[T <: Tuple]: List[ConfigReader[_]] =
      inline erasedValue[T] match {
        case _: (h *: t) =>
          (inline erasedValue[h] match {
            case ht: Tuple => deriveForTuple[ht.type] :: summonConfigReaders[t]
            case _ => summonConfigReader[h] :: summonConfigReaders[t]
          })

        case _: EmptyTuple => Nil
      }

    inline def summonConfigReader[A] =
      summonFrom {
        case reader: ConfigReader[A] => reader
        case given Mirror.Of[A] => ConfigReader.derived[A]
      }

    inline def deriveForTuple[T <: Tuple]: ConfigReader[T] =
      new ConfigReader[T] {
        def from(cur: ConfigCursor): ConfigReader.Result[T] =
          for {
            listCur <- asList(cur)
            result <- readTuple[T, 0](listCur.list)
          } yield result

        def asList(cur: ConfigCursor) =
          cur.asListCursor.flatMap { listCur =>
            if (constValue[Tuple.Size[T]] == listCur.size)
              Right(listCur)
            else
              listCur.failed(
                WrongSizeList(constValue[Tuple.Size[T]], listCur.size)
              )
          }
      }

    inline def readTuple[T <: Tuple, N <: Int](cursors: List[ConfigCursor]): Either[ConfigReaderFailures, T] =
      inline erasedValue[T] match {
        case _: (h *: t) =>
          val h = summonConfigReader[h].from(cursors(constValue[N]))

          h -> readTuple[t, N + 1](cursors) match {
            case (Right(h), Right(t)) => Right(widen[h *: t, T](h *: t))
            case (Left(h), Left(t)) => Left(h ++ t)
            case (_, Left(failures)) => Left(failures)
            case (Left(failures), _) => Left(failures)
          }

        case _: EmptyTuple =>
          Right(widen[EmptyTuple, T](EmptyTuple))
      }
  }
}
