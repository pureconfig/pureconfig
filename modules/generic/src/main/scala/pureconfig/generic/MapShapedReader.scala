package pureconfig.generic

import pureconfig._
import pureconfig.error._
import shapeless._
import shapeless.labelled.{ FieldType, field }

/**
 * A `ConfigReader` for generic representations that reads values in the shape of a config object.
 *
 * @tparam Wrapped the original type for which `Repr` is a generic sub-representation
 * @tparam Repr the generic representation
 */
trait MapShapedReader[Wrapped, Repr] extends ConfigReader[Repr]

object MapShapedReader {

  /**
   * A special form of `MapShapedReader` that includes usage of the field's default values.
   * @tparam Wrapped the original type for which `Repr` is a generic sub-representation
   * @tparam Repr the generic representation
   * @tparam DefaultRepr the generic representation of the default arguments
   */
  trait WithDefaults[Wrapped, Repr, DefaultRepr] {
    def fromWithDefault(cur: ConfigObjectCursor, default: DefaultRepr): ConfigReader.Result[Repr]
  }

  implicit def labelledHNilReader[Wrapped](
    implicit
    hint: ProductHint[Wrapped]): WithDefaults[Wrapped, HNil, HNil] = new WithDefaults[Wrapped, HNil, HNil] {

    def fromWithDefault(cur: ConfigObjectCursor, default: HNil): ConfigReader.Result[HNil] = {
      if (!hint.allowUnknownKeys && cur.keys.nonEmpty) {
        val keys = cur.map.toList.map { case (k, keyCur) => keyCur.failureFor(UnknownKey(k)) }
        Left(new ConfigReaderFailures(keys.head, keys.tail))
      } else {
        Right(HNil)
      }
    }
  }

  final implicit def labelledHConsReader[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit
    key: Witness.Aux[K],
    vFieldReader: Derivation[Lazy[ConfigReader[V]]],
    tConfigReader: Lazy[WithDefaults[Wrapped, T, U]],
    hint: ProductHint[Wrapped]): WithDefaults[Wrapped, FieldType[K, V] :: T, Option[V] :: U] = new WithDefaults[Wrapped, FieldType[K, V] :: T, Option[V] :: U] {

    def fromWithDefault(cur: ConfigObjectCursor, default: Option[V] :: U): ConfigReader.Result[FieldType[K, V] :: T] = {
      val fieldName = key.value.name
      val keyStr = hint.configKey(fieldName)

      val headReader = vFieldReader.value.value
      val headResult = cur.atKeyOrUndefined(keyStr) match {
        case keyCur if keyCur.isUndefined =>
          default.head match {
            case Some(defaultValue) if hint.useDefaultArgs => Right(defaultValue)
            case _ if headReader.isInstanceOf[ReadsMissingKeys] => headReader.from(keyCur)
            case _ => cur.failed(KeyNotFound.forKeys(keyStr, cur.keys))
          }
        case keyCur => headReader.from(keyCur)
      }
      // for performance reasons only, we shouldn't clone the config object unless necessary
      val tailCur = if (hint.allowUnknownKeys) cur else cur.withoutKey(keyStr)
      val tailResult = tConfigReader.value.fromWithDefault(tailCur, default.tail)
      ConfigReader.Result.zipWith(headResult, tailResult)((head, tail) => field[K](head) :: tail)
    }
  }

  implicit def cNilReader[Wrapped](
    implicit
    coproductHint: CoproductHint[Wrapped]): MapShapedReader[Wrapped, CNil] =
    new MapShapedReader[Wrapped, CNil] {
      override def from(cur: ConfigCursor): ConfigReader.Result[CNil] =
        Left(coproductHint.noOptionFound(cur))
    }

  final implicit def cConsReader[Wrapped, Name <: Symbol, V, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vFieldConvert: Derivation[Lazy[ConfigReader[V]]],
    tConfigReader: Lazy[MapShapedReader[Wrapped, T]]): MapShapedReader[Wrapped, FieldType[Name, V] :+: T] =
    new MapShapedReader[Wrapped, FieldType[Name, V] :+: T] {

      override def from(cur: ConfigCursor): ConfigReader.Result[FieldType[Name, V] :+: T] =
        coproductHint.from(cur, vName.value.name) match {
          case Right(Some(optCur)) =>
            vFieldConvert.value.value.from(optCur) match {
              case Left(_) if coproductHint.tryNextOnFail(vName.value.name) =>
                tConfigReader.value.from(cur).right.map(s => Inr(s))

              case vTry => vTry.right.map(v => Inl(field[Name](v)))
            }

          case Right(None) => tConfigReader.value.from(cur).right.map(s => Inr(s))
          case l: Left[_, _] => l.asInstanceOf[ConfigReader.Result[FieldType[Name, V] :+: T]]
        }
    }
}
