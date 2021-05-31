package pureconfig.generic

import shapeless._
import shapeless.labelled.{FieldType, field}

import pureconfig._
import pureconfig.error.KeyNotFound
import pureconfig.generic.ProductHint.UseOrDefault

/** A specialized reader for generic representations that reads values in the shape of a config object, and is capable
  * of handling default values.
  *
  * @tparam Original
  *   the original type for which `Repr` is a generic sub-representation
  * @tparam Repr
  *   the generic representation
  * @tparam DefaultRepr
  *   the default representation of the original type
  */
private[generic] trait MapShapedReader[Original, Repr, DefaultRepr] {
  def from(cur: ConfigObjectCursor, default: DefaultRepr, usedFields: Set[String]): ConfigReader.Result[Repr]
}

object MapShapedReader {

  implicit def labelledHNilReader[Original](implicit
      hint: ProductHint[Original]
  ): MapShapedReader[Original, HNil, HNil] =
    new MapShapedReader[Original, HNil, HNil] {
      def from(cur: ConfigObjectCursor, default: HNil, usedFields: Set[String]): ConfigReader.Result[HNil] =
        hint.bottom(cur, usedFields).fold[ConfigReader.Result[HNil]](Right(HNil))(Left.apply)
    }

  final implicit def labelledHConsReader[Original, K <: Symbol, H, T <: HList, D <: HList](implicit
      key: Witness.Aux[K],
      hConfigReader: Lazy[ConfigReader[H]],
      tConfigReader: Lazy[MapShapedReader[Original, T, D]],
      hint: ProductHint[Original]
  ): MapShapedReader[Original, FieldType[K, H] :: T, Option[H] :: D] =
    new MapShapedReader[Original, FieldType[K, H] :: T, Option[H] :: D] {
      def from(
          cur: ConfigObjectCursor,
          default: Option[H] :: D,
          usedFields: Set[String]
      ): ConfigReader.Result[FieldType[K, H] :: T] = {
        val fieldName = key.value.name
        val fieldAction = hint.from(cur, fieldName)
        lazy val reader = hConfigReader.value
        val headResult = (fieldAction, default.head) match {
          case (UseOrDefault(cursor, _), Some(defaultValue)) if cursor.isUndefined =>
            Right(defaultValue)
          case (action, _) if !action.cursor.isUndefined || reader.isInstanceOf[ReadsMissingKeys] =>
            reader.from(action.cursor)
          case _ =>
            cur.failed[H](KeyNotFound.forKeys(fieldAction.field, cur.keys))
        }
        val tailResult = tConfigReader.value.from(cur, default.tail, usedFields + fieldAction.field)
        ConfigReader.Result.zipWith(headResult, tailResult)((head, tail) => field[K](head) :: tail)
      }
    }
}
