package pureconfig.generic

import pureconfig.ConvertHelpers._
import pureconfig.error._
import pureconfig.{ ConfigCursor, ConfigReader, Derivation }
import shapeless._
import shapeless.ops.hlist.HKernelAux

trait SeqShapedReader[Repr] extends ConfigReader[Repr]

object SeqShapedReader {

  implicit val hNilReader: SeqShapedReader[HNil] = new SeqShapedReader[HNil] {
    def from(cur: ConfigCursor): Either[ConfigReaderFailures, HNil] = {
      cur.asList.right.flatMap {
        case Nil => Right(HNil)
        case cl => cur.failed(WrongSizeList(0, cl.size))
      }
    }
  }

  implicit def hConsReader[H, T <: HList](implicit hr: Derivation[Lazy[ConfigReader[H]]], tr: Lazy[SeqShapedReader[T]], tl: HKernelAux[T]): SeqShapedReader[H :: T] =
    new SeqShapedReader[H :: T] {
      def from(cur: ConfigCursor): Either[ConfigReaderFailures, H :: T] = {
        cur.asListCursor.right.flatMap {
          case listCur if listCur.size != tl().length + 1 =>
            cur.failed(WrongSizeList(tl().length + 1, listCur.size))

          case listCur =>
            // it's guaranteed that the list cursor is non-empty at this point due to the case above
            val hv = hr.value.value.from(listCur.atIndexOrUndefined(0))
            val tv = tr.value.from(listCur.tailOption.get)
            combineResults(hv, tv)(_ :: _)
        }
      }
    }
}
