package pureconfig.generic

import shapeless._
import shapeless.ops.hlist.HKernelAux

import pureconfig._
import pureconfig.error._

/** A `ConfigReader` for generic representations that reads values in the shape of a sequence.
  *
  * @tparam Repr
  *   the generic representation
  */
private[generic] trait SeqShapedReader[Repr] extends ConfigReader[Repr]

object SeqShapedReader {

  implicit val hNilReader: SeqShapedReader[HNil] = new SeqShapedReader[HNil] {
    def from(cur: ConfigCursor): ConfigReader.Result[HNil] = {
      cur.asList.flatMap {
        case Nil => Right(HNil)
        case cl => cur.failed(WrongSizeList(0, cl.size))
      }
    }
  }

  implicit def hConsReader[H, T <: HList](implicit
      hr: Lazy[ConfigReader[H]],
      tr: Lazy[SeqShapedReader[T]],
      tl: HKernelAux[T]
  ): SeqShapedReader[H :: T] =
    new SeqShapedReader[H :: T] {
      def from(cur: ConfigCursor): ConfigReader.Result[H :: T] = {
        cur.asListCursor.flatMap {
          case listCur if listCur.size != tl().length + 1 =>
            cur.failed(WrongSizeList(tl().length + 1, listCur.size))

          case listCur =>
            // it's guaranteed that the list cursor is non-empty at this point due to the case above
            val hv = hr.value.from(listCur.atIndexOrUndefined(0))
            val tv = tr.value.from(listCur.tailOption.get)
            ConfigReader.Result.zipWith(hv, tv)(_ :: _)
        }
      }
    }
}
