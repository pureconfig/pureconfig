package pureconfig

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.language.higherKinds

import com.typesafe.config._
import pureconfig.error._
import shapeless._
import shapeless.labelled._

/**
 * The default behavior of ConfigConverts that are implicitly derived in PureConfig is to raise a
 * KeyNotFoundException when a required key is missing. Mixing in this trait to a ConfigConvert
 * allows customizing this behavior. When a key is missing, but the ConfigConvert of the given
 * type extends this trait, the `from` method of the ConfigConvert is called with null.
 */
trait AllowMissingKey { self: ConfigConvert[_] => }

/**
 * Trait containing `ConfigConvert` instances for collection, product and coproduct types.
 */
trait DerivedConverters extends ConvertHelpers {

  private[pureconfig] trait WrappedConfigConvert[Wrapped, SubRepr] extends ConfigConvert[SubRepr]

  private[pureconfig] trait WrappedDefaultValue[Wrapped, SubRepr <: HList, DefaultRepr <: HList] {
    def fromWithDefault(config: ConfigValue, default: DefaultRepr): Either[ConfigReaderFailures, SubRepr] = config match {
      case co: ConfigObject => fromConfigObject(co, default)
      case other => fail(WrongType(s"${other.valueType}", s"${ConfigValueType.OBJECT}", ConfigValueLocation(other), None))
    }
    def fromConfigObject(co: ConfigObject, default: DefaultRepr): Either[ConfigReaderFailures, SubRepr]
    def to(v: SubRepr): ConfigValue
  }

  private[pureconfig] def improveFailures[Z](result: Either[ConfigReaderFailures, Z], keyStr: String, location: Option[ConfigValueLocation]): Either[ConfigReaderFailures, Z] =
    result.left.map {
      case ConfigReaderFailures(head, tail) =>
        val headImproved = head.withImprovedContext(keyStr, location)
        val tailImproved = tail.map(_.withImprovedContext(keyStr, location))
        ConfigReaderFailures(headImproved, tailImproved)
    }

  implicit final def hNilConfigConvert[Wrapped](
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

    override def to(t: HNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit final def hConsConfigConvert[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit
    key: Witness.Aux[K],
    vFieldConvert: Lazy[ConfigConvert[V]],
    tConfigConvert: Lazy[WrappedDefaultValue[Wrapped, T, U]],
    hint: ProductHint[Wrapped]): WrappedDefaultValue[Wrapped, FieldType[K, V] :: T, Option[V] :: U] = new WrappedDefaultValue[Wrapped, FieldType[K, V] :: T, Option[V] :: U] {

    override def fromConfigObject(co: ConfigObject, default: Option[V] :: U): Either[ConfigReaderFailures, FieldType[K, V] :: T] = {
      val keyStr = hint.configKey(key.value.toString().tail)
      val headResult = improveFailures[V](
        (co.get(keyStr), vFieldConvert.value) match {
          case (null, converter: AllowMissingKey) =>
            converter.from(co.get(keyStr))
          case (null, _) =>
            val defaultValue = if (hint.useDefaultArgs) default.head else None
            defaultValue.fold(fail[V](CannotConvertNull))(Right[Nothing, V](_))
          case (value, converter) =>
            converter.from(value)
        },
        keyStr,
        ConfigValueLocation(co))
      // for performance reasons only, we shouldn't clone the config object unless necessary
      val tailCo = if (hint.allowUnknownKeys) co else co.withoutKey(keyStr)
      val tailResult = tConfigConvert.value.fromWithDefault(tailCo, default.tail)
      combineResults(headResult, tailResult)((head, tail) => field[K](head) :: tail)
    }

    override def to(t: FieldType[K, V] :: T): ConfigValue = {
      val keyStr = hint.configKey(key.value.toString().tail)
      val rem = tConfigConvert.value.to(t.tail)
      // TODO check that all keys are unique
      vFieldConvert.value match {
        case f: OptionConfigConvert[_] =>
          f.toOption(t.head) match {
            case Some(v) =>
              rem.asInstanceOf[ConfigObject].withValue(keyStr, v)
            case None =>
              rem
          }
        case f =>
          val fieldEntry = f.to(t.head)
          rem.asInstanceOf[ConfigObject].withValue(keyStr, fieldEntry)
      }
    }
  }

  implicit final def cNilConfigConvert[Wrapped]: WrappedConfigConvert[Wrapped, CNil] = new WrappedConfigConvert[Wrapped, CNil] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, CNil] =
      fail(NoValidCoproductChoiceFound(config, ConfigValueLocation(config), None))

    override def to(t: CNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit final def coproductConfigConvert[Wrapped, Name <: Symbol, V, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vFieldConvert: Lazy[ConfigConvert[V]],
    tConfigConvert: Lazy[WrappedConfigConvert[Wrapped, T]]): WrappedConfigConvert[Wrapped, FieldType[Name, V] :+: T] =
    new WrappedConfigConvert[Wrapped, FieldType[Name, V] :+: T] {

      override def from(config: ConfigValue): Either[ConfigReaderFailures, FieldType[Name, V] :+: T] =
        coproductHint.from(config, vName.value.name) match {
          case Right(Some(hintConfig)) =>
            vFieldConvert.value.from(hintConfig) match {
              case Left(_) if coproductHint.tryNextOnFail(vName.value.name) =>
                tConfigConvert.value.from(config).right.map(s => Inr(s))

              case vTry => vTry.right.map(v => Inl(field[Name](v)))
            }

          case Right(None) => tConfigConvert.value.from(config).right.map(s => Inr(s))
          case l: Left[_, _] => l.asInstanceOf[Either[ConfigReaderFailures, FieldType[Name, V] :+: T]]
        }

      override def to(t: FieldType[Name, V] :+: T): ConfigValue = t match {
        case Inl(l) =>
          // Writing a coproduct to a config can fail. Is it worth it to make `to` return a `Try`?
          coproductHint.to(vFieldConvert.value.to(l), vName.value.name) match {
            case Left(failures) => throw new ConfigReaderException[FieldType[Name, V] :+: T](failures)
            case Right(r) => r
          }

        case Inr(r) =>
          tConfigConvert.value.to(r)
      }
    }

  // For Option[T] we use a special config converter
  implicit def deriveOption[T](implicit conv: Lazy[ConfigConvert[T]]) = new OptionConfigConvert[T]

  class OptionConfigConvert[T](implicit conv: Lazy[ConfigConvert[T]]) extends ConfigConvert[Option[T]] with AllowMissingKey {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Option[T]] = {
      if (config == null || config.unwrapped() == null)
        Right(None)
      else
        conv.value.from(config).right.map(Some(_))
    }

    override def to(t: Option[T]): ConfigValue = t match {
      case Some(v) => conv.value.to(v)
      case None => ConfigValueFactory.fromMap(Map().asJava)
    }

    def toOption(t: Option[T]): Option[ConfigValue] = t.map(conv.value.to)
  }

  // traversable of types with an instance of ConfigConvert
  implicit def deriveTraversable[T, F[T] <: TraversableOnce[T]](
    implicit
    configConvert: Lazy[ConfigConvert[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigConvert[F[T]] {

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
                s"$value which cannot converted. Error: ${t.because}", ConfigValueLocation(value), Some(key))))
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
          fail(WrongType(s"${other.valueType}", s"${ConfigValueType.LIST} or ${ConfigValueType.OBJECT}", ConfigValueLocation(other), None))
      }
    }

    override def to(ts: F[T]): ConfigValue = {
      ConfigValueFactory.fromIterable(ts.toList.map(configConvert.value.to).asJava)
    }
  }

  implicit def deriveMap[T](implicit configConvert: Lazy[ConfigConvert[T]]) = new ConfigConvert[Map[String, T]] {

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
          fail(WrongType(s"${other.valueType}", s"${ConfigValueType.OBJECT}", ConfigValueLocation(other), None))
      }
    }

    override def to(keyVals: Map[String, T]): ConfigValue = {
      ConfigValueFactory.fromMap(keyVals.mapValues(configConvert.value.to).asJava)
    }
  }

  // used for products
  implicit final def deriveProductInstance[F, Repr <: HList, DefaultRepr <: HList](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cc: Lazy[WrappedDefaultValue[F, Repr, DefaultRepr]]): ConfigConvert[F] = new ConfigConvert[F] {

    override def from(config: ConfigValue): Either[ConfigReaderFailures, F] = {
      cc.value.fromWithDefault(config, default()).right.map(gen.from)
    }

    override def to(t: F): ConfigValue = {
      cc.value.to(gen.to(t))
    }
  }

  // used for coproducts
  implicit final def deriveCoproductInstance[F, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[WrappedConfigConvert[F, Repr]]): ConfigConvert[F] = new ConfigConvert[F] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, F] = {
      cc.value.from(config).right.map(gen.from)
    }

    override def to(t: F): ConfigValue = {
      cc.value.to(gen.to(t))
    }
  }
}

object DerivedConverters extends DerivedConverters
