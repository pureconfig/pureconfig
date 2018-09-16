package pureconfig

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.language.higherKinds

import pureconfig.ConvertHelpers._
import pureconfig.error._
import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.HKernelAux

/**
 * The default behavior of ConfigReaders that are implicitly derived in PureConfig is to raise a
 * KeyNotFoundException when a required key is missing. Mixing in this trait to a ConfigReader
 * allows customizing this behavior. When a key is missing, but the ConfigReader of the given
 * type extends this trait, the `from` method of the ConfigReader is called with null.
 */
trait AllowMissingKey { self: ConfigReader[_] => }

/**
 * Trait extending [[DerivedReaders1]] that contains `ConfigReader` instances for `AnyVal`.
 *
 * This trait exists to give priority to the `AnyVal` derivation over the generic product derivation.
 */
trait DerivedReaders extends DerivedReaders1 {
  implicit def deriveAnyVal[T, U](
    implicit
    ev: T <:< AnyVal,
    generic: Generic[T],
    unwrapped: Unwrapped.Aux[T, U],
    reader: ConfigReader[U]): ConfigReader[T] =
    new ConfigReader[T] {
      def from(value: ConfigCursor): Either[ConfigReaderFailures, T] =
        reader.from(value).right.map(unwrapped.wrap)
    }

  // used for tuples
  implicit def deriveTupleInstance[F: IsTuple, Repr <: HList, LRepr <: HList, DefaultRepr <: HList](
    implicit
    g: Generic.Aux[F, Repr],
    gcr: ConfigReader[Repr],
    lg: LabelledGeneric.Aux[F, LRepr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    pr: WrappedDefaultValue[F, LRepr, DefaultRepr]): ConfigReader[F] = new ConfigReader[F] {
    override def from(cur: ConfigCursor) = {
      // Try to read first as the product representation (i.e.
      // ConfigObject with '_1', '_2', etc. keys) and afterwards as the Generic
      // representation (i.e. ConfigList).
      cur.asCollectionCursor.right.flatMap {
        case Right(objCur) => deriveTupleInstanceAsObject(objCur)
        case Left(_) => deriveTupleInstanceAsList(cur)
      }
    }
  }

  private[pureconfig] def deriveTupleInstanceAsList[F: IsTuple, Repr <: HList](cur: ConfigCursor)(
    implicit
    gen: Generic.Aux[F, Repr],
    cr: ConfigReader[Repr]): Either[ConfigReaderFailures, F] =
    cr.from(cur).right.map(gen.from)

  private[pureconfig] def deriveTupleInstanceAsObject[F: IsTuple, Repr <: HList, DefaultRepr <: HList](cur: ConfigObjectCursor)(
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cr: WrappedDefaultValue[F, Repr, DefaultRepr]): Either[ConfigReaderFailures, F] =
    cr.fromWithDefault(cur, default()).right.map(gen.from)
}

/**
 * Trait containing `ConfigReader` instances for collection, product and coproduct types.
 */
trait DerivedReaders1 {

  private[pureconfig] trait WrappedConfigReader[Wrapped, SubRepr] extends ConfigReader[SubRepr]

  protected[pureconfig] trait WrappedDefaultValue[Wrapped, SubRepr <: HList, DefaultRepr <: HList] {
    def fromWithDefault(cur: ConfigObjectCursor, default: DefaultRepr): Either[ConfigReaderFailures, SubRepr]
  }

  implicit final def labelledHNilConfigReader[Wrapped](
    implicit
    hint: ProductHint[Wrapped]): WrappedDefaultValue[Wrapped, HNil, HNil] = new WrappedDefaultValue[Wrapped, HNil, HNil] {

    def fromWithDefault(cur: ConfigObjectCursor, default: HNil): Either[ConfigReaderFailures, HNil] = {
      if (!hint.allowUnknownKeys && cur.keys.nonEmpty) {
        val keys = cur.map.toList.map { case (k, keyCur) => keyCur.failureFor(UnknownKey(k)) }
        Left(new ConfigReaderFailures(keys.head, keys.tail))
      } else {
        Right(HNil)
      }
    }
  }

  implicit final def labelledHConsConfigReader[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit
    key: Witness.Aux[K],
    vFieldReader: Derivation[Lazy[ConfigReader[V]]],
    tConfigReader: Lazy[WrappedDefaultValue[Wrapped, T, U]],
    hint: ProductHint[Wrapped]): WrappedDefaultValue[Wrapped, FieldType[K, V] :: T, Option[V] :: U] = new WrappedDefaultValue[Wrapped, FieldType[K, V] :: T, Option[V] :: U] {

    def fromWithDefault(cur: ConfigObjectCursor, default: Option[V] :: U): Either[ConfigReaderFailures, FieldType[K, V] :: T] = {
      val fieldName = key.value.name
      val keyStr = hint.configKey(fieldName)

      val headReader = vFieldReader.value.value
      val headResult = cur.atKeyOrUndefined(keyStr) match {
        case keyCur if keyCur.isUndefined =>
          default.head match {
            case Some(defaultValue) if hint.useDefaultArgs => Right(defaultValue)
            case _ if headReader.isInstanceOf[AllowMissingKey] => headReader.from(keyCur)
            case _ => cur.failed(KeyNotFound.forKeys(keyStr, cur.keys))
          }
        case keyCur => headReader.from(keyCur)
      }
      // for performance reasons only, we shouldn't clone the config object unless necessary
      val tailCur = if (hint.allowUnknownKeys) cur.withoutKey(keyStr) else cur.withoutKey(keyStr)
      val tailResult = tConfigReader.value.fromWithDefault(tailCur, default.tail)
      combineResults(headResult, tailResult)((head, tail) => field[K](head) :: tail)
    }
  }

  implicit final def cNilConfigReader[Wrapped]: WrappedConfigReader[Wrapped, CNil] = new WrappedConfigReader[Wrapped, CNil] {
    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, CNil] =
      cur.failed(NoValidCoproductChoiceFound(cur.value))
  }

  implicit final def coproductConfigReader[Wrapped, Name <: Symbol, V, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vFieldConvert: Derivation[Lazy[ConfigReader[V]]],
    tConfigReader: Lazy[WrappedConfigReader[Wrapped, T]]): WrappedConfigReader[Wrapped, FieldType[Name, V] :+: T] =
    new WrappedConfigReader[Wrapped, FieldType[Name, V] :+: T] {

      override def from(cur: ConfigCursor): Either[ConfigReaderFailures, FieldType[Name, V] :+: T] =
        coproductHint.from(cur, vName.value.name) match {
          case Right(Some(optCur)) =>
            vFieldConvert.value.value.from(optCur) match {
              case Left(_) if coproductHint.tryNextOnFail(vName.value.name) =>
                tConfigReader.value.from(cur).right.map(s => Inr(s))

              case vTry => vTry.right.map(v => Inl(field[Name](v)))
            }

          case Right(None) => tConfigReader.value.from(cur).right.map(s => Inr(s))
          case l: Left[_, _] => l.asInstanceOf[Either[ConfigReaderFailures, FieldType[Name, V] :+: T]]
        }
    }

  implicit def deriveOption[T](implicit conv: Derivation[Lazy[ConfigReader[T]]]) = new OptionConfigReader[T]

  class OptionConfigReader[T](implicit conv: Derivation[Lazy[ConfigReader[T]]]) extends ConfigReader[Option[T]] with AllowMissingKey {
    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, Option[T]] = {
      if (cur.isUndefined || cur.isNull) Right(None)
      else conv.value.value.from(cur).right.map(Some(_))
    }
  }

  implicit def deriveTraversable[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Derivation[Lazy[ConfigReader[T]]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigReader[F[T]] {

    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, F[T]] = {
      cur.asListCursor.right.flatMap { listCur =>
        // we called all the failures in the list
        listCur.list.foldLeft[Either[ConfigReaderFailures, mutable.Builder[T, F[T]]]](Right(cbf())) {
          case (acc, valueCur) =>
            combineResults(acc, configConvert.value.value.from(valueCur))(_ += _)
        }.right.map(_.result())
      }
    }
  }

  implicit def deriveMap[T](implicit reader: Derivation[Lazy[ConfigReader[T]]]) = new ConfigReader[Map[String, T]] {
    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, Map[String, T]] = {
      cur.asMap.right.flatMap { map =>
        map.foldLeft[Either[ConfigReaderFailures, Map[String, T]]](Right(Map())) {
          case (acc, (key, valueConf)) =>
            combineResults(acc, reader.value.value.from(valueConf)) { (map, value) => map + (key -> value) }
        }
      }
    }
  }

  implicit final lazy val hNilConfigReader: ConfigReader[HNil] =
    new ConfigReader[HNil] {
      def from(cur: ConfigCursor): Either[ConfigReaderFailures, HNil] = {
        cur.asList.right.flatMap {
          case Nil => Right(HNil)
          case cl => cur.failed(WrongSizeList(0, cl.size))
        }
      }
    }

  implicit final def hConsConfigReader[H, T <: HList](implicit hr: Derivation[Lazy[ConfigReader[H]]], tr: Lazy[ConfigReader[T]], tl: HKernelAux[T]): ConfigReader[H :: T] =
    new ConfigReader[H :: T] {
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

  implicit final def deriveProductInstance[F, Repr <: HList, DefaultRepr <: HList](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cc: Lazy[WrappedDefaultValue[F, Repr, DefaultRepr]]): ConfigReader[F] = new ConfigReader[F] {

    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, F] = {
      cur.asObjectCursor.right.flatMap(cc.value.fromWithDefault(_, default())).right.map(gen.from)
    }
  }

  implicit final def deriveCoproductInstance[F, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[WrappedConfigReader[F, Repr]]): ConfigReader[F] = new ConfigReader[F] {
    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, F] = {
      cc.value.from(cur).right.map(gen.from)
    }
  }

}

object DerivedReaders extends DerivedReaders
