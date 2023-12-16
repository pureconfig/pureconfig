package pureconfig
package generic
package derivation

import scala.compiletime.ops.int._
import scala.compiletime.{constValue, constValueTuple, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror
import scala.util.chaining.*

import pureconfig.error.{ConfigReaderFailures, ConvertFailure, KeyNotFound, UnknownKey, WrongSizeList}
import pureconfig.generic.derivation.Utils._

trait ProductConfigReaderDerivation(fieldMapping: ConfigFieldMapping) { self: ConfigReaderDerivation =>
  inline def derivedProduct[A](using m: Mirror.ProductOf[A]): ConfigReader[A] =
    inline erasedValue[A] match {
      case _: Tuple =>
        new ConfigReader[A] {
          def from(cur: ConfigCursor): ConfigReader.Result[A] =
            for {
              listCur <- asList(cur)
              result <- readTuple[A & Tuple, 0](listCur.list, Nil)
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
              objCur <- cur.asObjectCursor
              result <-
                transformedLabels[A](fieldMapping)
                  .pipe(labels =>
                    readTuple[m.MirroredElemTypes, 0](
                      labels.map(objCur.atKeyOrUndefined(_)),
                      labels.map(KeyNotFound.forKeys(_, objCur.keys))
                    )
                  )
            } yield m.fromProduct(result)
        }
    }

  inline def readTuple[T <: Tuple, N <: Int](
      cursors: List[ConfigCursor],
      keyNotFound: List[KeyNotFound]
  ): Either[ConfigReaderFailures, T] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val reader = summonConfigReader[h]
        val cursor = cursors(constValue[N])
        val h =
          (reader.isInstanceOf[ReadsMissingKeys], cursor.isUndefined) match {
            case (true, true) | (_, false) => reader.from(cursor)
            case (false, true) => cursor.failed(keyNotFound(constValue[N]))
          }

        h -> readTuple[t, N + 1](cursors, keyNotFound) match {
          case (Right(h), Right(t)) => Right(widen[h *: t, T](h *: t))
          case (Left(h), Left(t)) => Left(h ++ t)
          case (_, Left(failures)) => Left(failures)
          case (Left(failures), _) => Left(failures)
        }

      case _: EmptyTuple =>
        Right(widen[EmptyTuple, T](EmptyTuple))
    }
}
