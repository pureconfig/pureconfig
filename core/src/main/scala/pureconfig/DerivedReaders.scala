package pureconfig

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.language.higherKinds

import com.typesafe.config._
import pureconfig.ConvertHelpers._
import pureconfig.error._
import shapeless._
import shapeless.labelled._

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
      def from(value: ConfigValue): Either[ConfigReaderFailures, T] =
        reader.from(value).right.map(unwrapped.wrap)
    }
}

/**
 * Trait containing `ConfigReader` instances for collection, product and coproduct types.
 */
trait DerivedReaders1 {

  private[pureconfig] trait WrappedConfigReader[Wrapped, SubRepr] extends ConfigReader[SubRepr]

  private[pureconfig] trait WrappedDefaultValue[Wrapped, SubRepr <: HList, DefaultRepr <: HList] {
    def fromWithDefault(config: ConfigValue, default: DefaultRepr): Either[ConfigReaderFailures, SubRepr] = config match {
      case co: ConfigObject => fromConfigObject(co, default)
      case other => fail(WrongType(other.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(other), ""))
    }
    def fromConfigObject(co: ConfigObject, default: DefaultRepr): Either[ConfigReaderFailures, SubRepr]
  }

  implicit final def hNilConfigReader[Wrapped](
    implicit
    hint: ProductHint[Wrapped]): WrappedDefaultValue[Wrapped, HNil, HNil] = new WrappedDefaultValue[Wrapped, HNil, HNil] {

    override def fromConfigObject(config: ConfigObject, default: HNil): Either[ConfigReaderFailures, HNil] = {
      if (!hint.allowUnknownKeys && !config.isEmpty) {
        val keys = config.keySet().asScala.toList map {
          k => UnknownKey(k, ConfigValueLocation(config.get(k)))
        }
        Left(new ConfigReaderFailures(keys.head, keys.tail))
      } else {
        Right(HNil)
      }
    }
  }

  implicit final def hConsConfigReader[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit
    key: Witness.Aux[K],
    vFieldReader: Lazy[ConfigReader[V]],
    tConfigReader: Lazy[WrappedDefaultValue[Wrapped, T, U]],
    hint: ProductHint[Wrapped]): WrappedDefaultValue[Wrapped, FieldType[K, V] :: T, Option[V] :: U] = new WrappedDefaultValue[Wrapped, FieldType[K, V] :: T, Option[V] :: U] {

    override def fromConfigObject(co: ConfigObject, default: Option[V] :: U): Either[ConfigReaderFailures, FieldType[K, V] :: T] = {
      val fieldName = key.value.name
      val keyStr = hint.configKey(fieldName)
      val headResult = improveFailures[V](
        (co.get(keyStr), vFieldReader.value) match {
          case (null, reader) =>
            default.head match {
              case Some(defaultValue) if hint.useDefaultArgs => Right[Nothing, V](defaultValue)
              case _ if reader.isInstanceOf[AllowMissingKey] => reader.from(null)
              case _ => fail[V](CannotConvertNull(fieldName, co.keySet.asScala))
            }
          case (value, reader) => reader.from(value)
        },
        keyStr,
        ConfigValueLocation(co))
      // for performance reasons only, we shouldn't clone the config object unless necessary
      val tailCo = if (hint.allowUnknownKeys) co else co.withoutKey(keyStr)
      val tailResult = tConfigReader.value.fromWithDefault(tailCo, default.tail)
      combineResults(headResult, tailResult)((head, tail) => field[K](head) :: tail)
    }
  }

  implicit final def cNilConfigReader[Wrapped]: WrappedConfigReader[Wrapped, CNil] = new WrappedConfigReader[Wrapped, CNil] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, CNil] =
      fail(NoValidCoproductChoiceFound(config, ConfigValueLocation(config), ""))
  }

  implicit final def coproductConfigReader[Wrapped, Name <: Symbol, V, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vFieldConvert: Lazy[ConfigReader[V]],
    tConfigReader: Lazy[WrappedConfigReader[Wrapped, T]]): WrappedConfigReader[Wrapped, FieldType[Name, V] :+: T] =
    new WrappedConfigReader[Wrapped, FieldType[Name, V] :+: T] {

      override def from(config: ConfigValue): Either[ConfigReaderFailures, FieldType[Name, V] :+: T] =
        coproductHint.from(config, vName.value.name) match {
          case Right(Some(hintConfig)) =>
            vFieldConvert.value.from(hintConfig) match {
              case Left(_) if coproductHint.tryNextOnFail(vName.value.name) =>
                tConfigReader.value.from(config).right.map(s => Inr(s))

              case vTry => vTry.right.map(v => Inl(field[Name](v)))
            }

          case Right(None) => tConfigReader.value.from(config).right.map(s => Inr(s))
          case l: Left[_, _] => l.asInstanceOf[Either[ConfigReaderFailures, FieldType[Name, V] :+: T]]
        }
    }

  implicit def deriveOption[T](implicit conv: Lazy[ConfigReader[T]]) = new OptionConfigReader[T]

  class OptionConfigReader[T](implicit conv: Lazy[ConfigReader[T]]) extends ConfigReader[Option[T]] with AllowMissingKey {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Option[T]] = {
      if (config == null || config.unwrapped() == null)
        Right(None)
      else
        conv.value.from(config).right.map(Some(_))
    }
  }

  implicit def deriveTraversable[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Lazy[ConfigReader[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigReader[F[T]] {

    override def from(config: ConfigValue): Either[ConfigReaderFailures, F[T]] = {
      config match {
        case co: ConfigList =>
          val z: Either[ConfigReaderFailures, Builder[T, F[T]]] = Right(cbf())

          // we called all the failures in the list
          co.asScala.foldLeft(z) {
            case (acc, value) =>
              combineResults(acc, configConvert.value.from(value))(_ += _)
          }.right.map(_.result())
        case o: ConfigObject =>
          val z: Either[ConfigReaderFailures, List[(Int, T)]] = Right(List.empty[(Int, T)])
          def keyValueReader(key: String, value: ConfigValue): Either[ConfigReaderFailures, (Int, T)] = {
            val keyResult = catchReadError(_.toInt)(implicitly)(key)(ConfigValueLocation(value)).left.flatMap(t => fail(CannotConvert(key, "Int",
              s"To convert an object to a collection, its keys must be read as Int but key $key has value" +
                s"$value which cannot converted. Error: ${t.because}", ConfigValueLocation(value), key)))
            val valueResult = configConvert.value.from(value)
            combineResults(keyResult, valueResult)(_ -> _)
          }

          o.asScala.foldLeft(z) {
            case (acc, (str, v)) =>
              combineResults(acc, keyValueReader(str, v))(_ :+ _)
          }.right.map {
            l =>
              val r = cbf()
              r ++= l.sortBy(_._1).map(_._2)
              r.result()
          }
        case other =>
          fail(WrongType(other.valueType, Set(ConfigValueType.LIST, ConfigValueType.OBJECT), ConfigValueLocation(other), ""))
      }
    }
  }

  implicit def deriveMap[T](implicit configConvert: Lazy[ConfigReader[T]]) = new ConfigReader[Map[String, T]] {

    override def from(config: ConfigValue): Either[ConfigReaderFailures, Map[String, T]] = {
      config match {
        case co: ConfigObject =>
          val z: Either[ConfigReaderFailures, Map[String, T]] = Right(Map.empty[String, T])

          co.asScala.foldLeft(z) {
            case (acc, (key, value)) =>
              combineResults(
                acc,
                improveFailures(configConvert.value.from(value), key, ConfigValueLocation(value))) {
                  (map, valueConverted) => map + (key -> valueConverted)
                }
          }

        case other =>
          fail(WrongType(other.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(other), ""))
      }
    }
  }

  implicit final def deriveProductInstance[F, Repr <: HList, DefaultRepr <: HList](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cc: Lazy[WrappedDefaultValue[F, Repr, DefaultRepr]]): ConfigReader[F] = new ConfigReader[F] {

    override def from(config: ConfigValue): Either[ConfigReaderFailures, F] = {
      cc.value.fromWithDefault(config, default()).right.map(gen.from)
    }
  }

  implicit final def deriveCoproductInstance[F, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[WrappedConfigReader[F, Repr]]): ConfigReader[F] = new ConfigReader[F] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, F] = {
      cc.value.from(config).right.map(gen.from)
    }
  }

}

object DerivedReaders extends DerivedReaders
