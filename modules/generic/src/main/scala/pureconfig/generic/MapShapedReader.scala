package pureconfig.generic

import pureconfig._
import pureconfig.error.{ ConfigReaderFailures, KeyNotFound }
import pureconfig.generic.CoproductHint.{ Attempt, Skip, Use }
import pureconfig.generic.ProductHint.UseOrDefault
import shapeless._
import shapeless.labelled.{ FieldType, field }

/**
 * A `ConfigReader` for generic representations that reads values in the shape of a config object.
 *
 * @tparam Wrapped the original type for which `Repr` is a generic sub-representation
 * @tparam Repr the generic representation
 */
private[generic] trait MapShapedReader[Wrapped, Repr] {
  def from(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReader.Result[Repr]
}

object MapShapedReader {

  /**
   * A special form of `MapShapedReader` that includes usage of the field's default values.
   * @tparam Wrapped the original type for which `Repr` is a generic sub-representation
   * @tparam Repr the generic representation
   * @tparam DefaultRepr the generic representation of the default arguments
   */
  private[generic] trait WithDefaults[Wrapped, Repr, DefaultRepr] {
    def fromWithDefault(cur: ConfigObjectCursor, default: DefaultRepr, usedFields: Set[String]): ConfigReader.Result[Repr]
  }

  implicit def labelledHNilReader[Wrapped](
    implicit
    hint: ProductHint[Wrapped]): WithDefaults[Wrapped, HNil, HNil] = new WithDefaults[Wrapped, HNil, HNil] {

    def fromWithDefault(cur: ConfigObjectCursor, default: HNil, usedFields: Set[String]): ConfigReader.Result[HNil] =
      hint.bottom(cur, usedFields).fold[ConfigReader.Result[HNil]](Right(HNil))(Left.apply)
  }

  final implicit def labelledHConsReader[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit
    key: Witness.Aux[K],
    vFieldReader: Derivation[Lazy[ConfigReader[V]]],
    tConfigReader: Lazy[WithDefaults[Wrapped, T, U]],
    hint: ProductHint[Wrapped]): WithDefaults[Wrapped, FieldType[K, V] :: T, Option[V] :: U] = new WithDefaults[Wrapped, FieldType[K, V] :: T, Option[V] :: U] {

    def fromWithDefault(cur: ConfigObjectCursor, default: Option[V] :: U, usedFields: Set[String]): ConfigReader.Result[FieldType[K, V] :: T] = {
      val fieldName = key.value.name
      val fieldAction = hint.from(cur, fieldName)
      lazy val reader = vFieldReader.value.value
      lazy val keyNotFoundFailure = cur.failed[V](KeyNotFound.forKeys(fieldAction.field, cur.keys))
      val headResult = (fieldAction, default.head) match {
        case (UseOrDefault(cursor, _), Some(defaultValue)) if cursor.isUndefined =>
          Right(defaultValue)
        case (action, _) if reader.isInstanceOf[ReadsMissingKeys] || !action.cursor.isUndefined =>
          reader.from(action.cursor)
        case _ =>
          keyNotFoundFailure
      }
      val tailResult = tConfigReader.value.fromWithDefault(cur, default.tail, usedFields + fieldAction.field)
      ConfigReader.Result.zipWith(headResult, tailResult)((head, tail) => field[K](head) :: tail)
    }
  }

  implicit def cNilReader[Wrapped](
    implicit
    coproductHint: CoproductHint[Wrapped]): MapShapedReader[Wrapped, CNil] =
    new MapShapedReader[Wrapped, CNil] {
      override def from(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReader.Result[CNil] =
        Left(coproductHint.bottom(cur, attempts))
    }

  final implicit def cConsReader[Wrapped, Name <: Symbol, V <: Wrapped, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vConfigReader: Derivation[Lazy[ConfigReader[V]]],
    tConfigReader: Lazy[MapShapedReader[Wrapped, T]]): MapShapedReader[Wrapped, FieldType[Name, V] :+: T] =
    new MapShapedReader[Wrapped, FieldType[Name, V] :+: T] {

      override def from(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReader.Result[FieldType[Name, V] :+: T] = {
        lazy val vReader = vConfigReader.value.value
        lazy val tReader = tConfigReader.value

        coproductHint.from(cur, vName.value.name).right.flatMap {
          case Use(optCur) =>
            vReader.from(optCur)
              .right.map(v => Inl(field[Name](v)))
              .left.map(failures => coproductHint.bottom(cur, attempts :+ (vName.value.name -> failures)))
          case Attempt(optCur) =>
            vReader.from(optCur)
              .right.map(v => Inl(field[Name](v)))
              .left.flatMap(failures => tReader.from(optCur, attempts :+ (vName.value.name -> failures)).right.map(Inr.apply))
          case Skip =>
            tReader.from(cur, attempts).right.map(Inr.apply)
        }
      }
    }
}
