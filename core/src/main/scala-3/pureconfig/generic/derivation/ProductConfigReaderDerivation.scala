package pureconfig
package generic
package derivation

import scala.compiletime.ops.int.*
import scala.compiletime.{constValue, constValueTuple, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror
import pureconfig.error._
import pureconfig.generic.derivation.Utils._

@deprecated(
  "Custom derivation is deprecated in pureconfig-core. If you only need the default behavior, please use the default `derives` behavior. If you need configuration please use the `pureconfig-generic-scala3` module instead.",
  "0.17.7"
)
trait ProductConfigReaderDerivation(fieldMapping: ConfigFieldMapping) { self: ConfigReaderDerivation =>

  inline def derivedProduct[A](using m: Mirror.ProductOf[A]): ConfigReader[A] =
    inline erasedValue[A] match {
      case _: Tuple =>
        // Deriving reader for a Scala tuple: read config as a list
        new ConfigReader[A] {
          def from(cur: ConfigCursor): ConfigReader.Result[A] =
            for {
              listCur <- asList(cur)
              result <- readTuple[A & Tuple](listCur.list)
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
        // Deriving reader for a case class: read config as an object
        new ConfigReader[A] {
          def from(cur: ConfigCursor): ConfigReader.Result[A] =
            for {
              objCur <- cur.asObjectCursor
              result <- {
                val labels = transformedLabels[A](fieldMapping)
                readTuple[m.MirroredElemTypes](labels.map(objCur.atKeyOrUndefined(_)), objCur.keys)
              }
            } yield m.fromTuple(result)
        }
    }

  /** Reads a typed `Tuple` out of a list of cursors (which must have the same size).
    *
    * @param cursors
    *   the list of cursors to read from
    * @param objKeys
    *   an optional list of parent object keys. Used only for better error messages and only when `undefined` cursor
    *   values are expected.
    */
  inline def readTuple[T <: Tuple](
      cursors: List[ConfigCursor],
      objKeys: Iterable[String] = Iterable.empty
  ): ConfigReader.Result[T] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val reader = summonConfigReader[h]
        val cur = cursors.head
        val hRes =
          (reader.isInstanceOf[ReadsMissingKeys], cur.isUndefined) match {
            case (true, true) | (_, false) => reader.from(cur)
            case (false, true) => cur.failed(KeyNotFound.forKeys(cur.pathElems.head, objKeys))
          }
        val tRes = readTuple[t](cursors.tail, objKeys)
        ConfigReader.Result.zipWith(hRes, tRes) { (hVal, tVal) => widen[h *: t, T](hVal *: tVal) }

      case _: EmptyTuple =>
        Right(widen[EmptyTuple, T](EmptyTuple))
    }
}
