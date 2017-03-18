/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import com.typesafe.config._
import shapeless._
import shapeless.labelled._
import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }
import java.net.{ URI, URL }
import java.nio.file.{ Path, Paths }
import java.time._
import java.util.UUID

import scala.concurrent.duration.{ Duration, FiniteDuration }
import pureconfig.ConfigConvert._
import pureconfig.error._
import scala.collection.mutable.Builder
import scala.util.control.NonFatal

/**
 * Trait for conversion between `T` and `ConfigValue`.
 */
trait ConfigConvert[T] {
  /**
   * Convert the given configuration into an instance of `T` if possible.
   *
   * @param config The configuration from which load the config
   * @return either a list of failures or an object of type `T`
   */
  def from(config: ConfigValue): Either[ConfigReaderFailures, T]

  /**
   * Converts a type `T` to a `ConfigValue`.
   *
   * @param t The instance of `T` to convert
   * @return The `ConfigValue` obtained from the `T` instance
   */
  def to(t: T): ConfigValue
}

/**
 * The default behavior of ConfigConverts that are implicitly derived in PureConfig is to raise a
 * KeyNotFoundException when a required key is missing. Mixing in this trait to a ConfigConvert
 * allows customizing this behavior. When a key is missing, but the ConfigConvert of the given
 * type extends this trait, the `from` method of the ConfigConvert is called with null.
 */
trait AllowMissingKey { self: ConfigConvert[_] => }

object ConfigConvert extends LowPriorityConfigConvertImplicits {

  def apply[T](implicit conv: ConfigConvert[T]): ConfigConvert[T] = conv

  def combineResults[A, B, C](first: Either[ConfigReaderFailures, A], second: Either[ConfigReaderFailures, B])(f: (A, B) => C): Either[ConfigReaderFailures, C] =
    (first, second) match {
      case (Right(a), Right(b)) => Right(f(a, b))
      case (Left(aFailures), Left(bFailures)) => Left(aFailures ++ bFailures)
      case (_, l: Left[_, _]) => l.asInstanceOf[Left[ConfigReaderFailures, Nothing]]
      case (l: Left[_, _], _) => l.asInstanceOf[Left[ConfigReaderFailures, Nothing]]
    }

  def fail[A](failure: ConfigReaderFailure): Either[ConfigReaderFailures, A] = Left(ConfigReaderFailures(failure))

  def failWithThrowable[A](throwable: Throwable): Option[ConfigValueLocation] => Either[ConfigReaderFailures, A] = location => fail[A](ThrowableFailure(throwable, location))

  private def eitherToResult[T](either: Either[ConfigReaderFailure, T]): Either[ConfigReaderFailures, T] =
    either match {
      case r: Right[_, _] => r.asInstanceOf[Either[ConfigReaderFailures, T]]
      case Left(failure) => Left(ConfigReaderFailures(failure))
    }

  private def tryToEither[T](t: Try[T]): Option[ConfigValueLocation] => Either[ConfigReaderFailure, T] = t match {
    case Success(t) => _ => Right(t)
    case Failure(e) => location => Left(ThrowableFailure(e, location))
  }

  private def stringToTryConvert[T](fromF: String => Try[T]): ConfigValue => Either[ConfigReaderFailures, T] =
    stringToEitherConvert[T](string => location => tryToEither(fromF(string))(location))

  private def stringToEitherConvert[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T]): ConfigValue => Either[ConfigReaderFailures, T] =
    config => {
      // Because we can't trust Typesafe Config not to throw, we wrap the
      // evaluation into a `try-catch` to prevent an unintentional exception from escaping.
      try {
        val string = config.valueType match {
          case ConfigValueType.STRING => config.unwrapped.toString
          case _ => config.render(ConfigRenderOptions.concise)
        }
        eitherToResult(fromF(string)(ConfigValueLocation(config)))
      } catch {
        case NonFatal(t) => failWithThrowable(t)(ConfigValueLocation(config))
      }
    }

  private def ensureNonEmpty[T](implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, String] = {
    case "" => location => Left(EmptyStringFound(ct.toString(), location))
    case x => _ => Right(x)
  }

  def catchReadError[T](f: String => T)(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[CannotConvert, T] =
    string => location =>
      try (Right(f(string))) catch {
        case NonFatal(ex) => Left(CannotConvert(string, ct.toString(), ex.toString, location))
      }

  /**
   * Convert a `String => Try` into a  `String => Option[ConfigValueLocation] => Either` such that after application
   * - `Success(t)` becomes `_ => Right(t)`
   * - `Failure(e)` becomes `location => Left(CannotConvert(value, type, e.getMessage, location)`
   */
  def tryF[T](f: String => Try[T])(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[CannotConvert, T] =
    string => location =>
      f(string) match {
        case Success(t) => Right(t)
        case Failure(e) => Left(CannotConvert(string, ct.runtimeClass.getName, e.getLocalizedMessage, location))
      }

  /**
   * Convert a `String => Option` into a `String => Option[ConfigValueLocation] => Either` such that after application
   * - `Some(t)` becomes `_ => Right(t)`
   * - `None` becomes `location => Left(CannotConvert(value, type, "", location)`
   */
  def optF[T](f: String => Option[T])(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[CannotConvert, T] =
    string => location =>
      f(string) match {
        case Some(t) => Right(t)
        case None => Left(CannotConvert(string, ct.runtimeClass.getName, "", location))
      }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromStringReader instead", since = "0.6.0")
  def fromString[T](fromF: String => Try[T]): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToTryConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  def fromStringReader[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T]): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToEitherConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  def fromStringReaderTry[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringReader[T](tryF(fromF))
  }

  def fromStringReaderOpt[T](fromF: String => Option[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringReader[T](optF(fromF))
  }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromNonEmptyStringReader instead", since = "0.6.0")
  def fromNonEmptyString[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromNonEmptyStringReader[T](fromF andThen tryToEither)
  }

  def fromNonEmptyStringReader[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringReader(string => location => ensureNonEmpty(ct)(string)(location).right.flatMap(s => fromF(s)(location)))
  }

  def fromNonEmptyStringReaderTry[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromNonEmptyStringReader[T](tryF(fromF))
  }

  def fromNonEmptyStringReaderOpt[T](fromF: String => Option[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromNonEmptyStringReader[T](optF(fromF))
  }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromStringConvert instead", since = "0.6.0")
  def stringConvert[T](fromF: String => Try[T], toF: T => String): ConfigConvert[T] =
    fromStringConvert[T](fromF andThen tryToEither, toF)

  def fromStringConvert[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToEitherConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(toF(t))
  }

  def fromStringConvertTry[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringConvert[T](tryF(fromF), toF)
  }

  def fromStringConvertOpt[T](fromF: String => Option[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringConvert[T](optF(fromF), toF)
  }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromNonEmptyStringConvert instead", since = "0.6.0")
  def nonEmptyStringConvert[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] =
    fromNonEmptyStringConvert[T](fromF andThen tryToEither[T], toF)

  def fromNonEmptyStringConvert[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringConvert[T](string => location => ensureNonEmpty(ct)(string)(location).right.flatMap(s => fromF(s)(location)), toF)
  }

  def fromNonEmptyStringConvertTry[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]) = {
    fromNonEmptyStringConvert[T](tryF(fromF), toF)
  }

  def fromNonEmptyStringConvertOpt[T](fromF: String => Option[T], toF: T => String)(implicit ct: ClassTag[T]) = {
    fromNonEmptyStringConvert[T](optF(fromF), toF)
  }

  private[pureconfig] trait WrappedConfigConvert[Wrapped, SubRepr] extends ConfigConvert[SubRepr]

  private[pureconfig] trait WrappedDefaultValue[Wrapped, SubRepr <: HList, DefaultRepr <: HList] {
    def fromWithDefault(config: ConfigValue, default: DefaultRepr): Either[ConfigReaderFailures, SubRepr] = config match {
      case co: ConfigObject => fromConfigObject(co, default)
      case other => fail(WrongType(foundTyp = other.valueType().toString, expectedTyp = "ConfigObject", ConfigValueLocation(other)))
    }
    def fromConfigObject(co: ConfigObject, default: DefaultRepr): Either[ConfigReaderFailures, SubRepr]
    def to(v: SubRepr): ConfigValue
  }

  implicit def hNilConfigConvert[Wrapped](
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

  private[pureconfig] def improveFailure[Z](failure: ConfigReaderFailure, keyStr: String, location: Option[ConfigValueLocation]): ConfigReaderFailure =
    failure match {
      case CannotConvertNull => KeyNotFound(keyStr, location)
      case CollidingKeys(suffix, existingValue, location) => CollidingKeys(keyStr + "." + suffix, existingValue, location)
      case WrongType(foundTyp, expectedTyp, location) => WrongTypeForKey(foundTyp, expectedTyp, keyStr, location)
      case WrongTypeForKey(foundTyp, expectedTyp, suffix, location) => WrongTypeForKey(foundTyp, expectedTyp, keyStr + "." + suffix, location)
      case UnknownKey(suffix, location) => UnknownKey(keyStr + "." + suffix, location)
      case KeyNotFound(suffix, location) => KeyNotFound(keyStr + "." + suffix, location)
      case e: ConfigReaderFailure => e
    }

  private[pureconfig] def improveFailures[Z](result: Either[ConfigReaderFailures, Z], keyStr: String, location: Option[ConfigValueLocation]): Either[ConfigReaderFailures, Z] =
    result.left.map {
      case ConfigReaderFailures(head, tail) =>
        val headImproved = improveFailure[Z](head, keyStr, location)
        val tailImproved = tail.map(improveFailure[Z](_, keyStr, location))
        ConfigReaderFailures(headImproved, tailImproved)
    }

  implicit def hConsConfigConvert[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
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

  implicit def cNilConfigConvert[Wrapped]: WrappedConfigConvert[Wrapped, CNil] = new WrappedConfigConvert[Wrapped, CNil] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, CNil] =
      fail(NoValidCoproductChoiceFound(config, ConfigValueLocation(config)))

    override def to(t: CNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit def coproductConfigConvert[Wrapped, Name <: Symbol, V, T <: Coproduct](
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
              s"To convert an object to a collection, it's keys must be read as Int but key $key has value" +
                s"$value which cannot converted. Error: ${t.because}", ConfigValueLocation(value))))
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
          fail(WrongType(other.valueType().toString, "ConfigList or ConfigObject", ConfigValueLocation(other)))
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
              combineResults(acc, configConvert.value.from(value))((map, valueConverted) => map + (key -> valueConverted))
          }

        case other =>
          fail(WrongType(other.valueType().toString, "ConfigObject", ConfigValueLocation(other)))
      }
    }

    override def to(keyVals: Map[String, T]): ConfigValue = {
      ConfigValueFactory.fromMap(keyVals.mapValues(configConvert.value.to).asJava)
    }
  }

  // used for products
  implicit def deriveProductInstance[F, Repr <: HList, DefaultRepr <: HList](
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
  implicit def deriveCoproductInstance[F, Repr <: Coproduct](
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

/**
 * Implicit [[ConfigConvert]] instances defined such that they can be overriden by library consumer via a locally defined implementation.
 */
trait LowPriorityConfigConvertImplicits {
  implicit val durationConfigConvert: ConfigConvert[Duration] = {
    fromNonEmptyStringConvert[Duration](DurationConvert.fromString, DurationConvert.fromDuration)
  }

  implicit val finiteDurationConfigConvert: ConfigConvert[FiniteDuration] = {
    val fromString: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, FiniteDuration] = { string => location =>
      DurationConvert.fromString(string)(location).right.flatMap {
        case d: FiniteDuration => Right(d)
        case _ => Left(CannotConvert(string, "FiniteDuration", s"Couldn't parse '$string' into a FiniteDuration because it's infinite.", location))
      }
    }
    fromNonEmptyStringConvert[FiniteDuration](fromString, DurationConvert.fromDuration)
  }

  implicit val instantConfigConvert: ConfigConvert[Instant] =
    fromNonEmptyStringConvert[Instant](catchReadError(Instant.parse), _.toString)

  implicit val zoneOffsetConfigConvert: ConfigConvert[ZoneOffset] =
    fromNonEmptyStringConvert[ZoneOffset](catchReadError(ZoneOffset.of), _.toString)

  implicit val zoneIdConfigConvert: ConfigConvert[ZoneId] =
    fromNonEmptyStringConvert[ZoneId](catchReadError(ZoneId.of), _.toString)

  implicit val periodConfigConvert: ConfigConvert[Period] =
    fromNonEmptyStringConvert[Period](catchReadError(Period.parse), _.toString)

  // see documentation for [[java.time.Year.parse]]
  private def yearToString(year: Year): String =
    if (year.getValue > 9999) "+" + year else year.toString

  implicit val yearConfigConvert: ConfigConvert[Year] =
    fromNonEmptyStringConvert[Year](catchReadError(Year.parse), yearToString)

  implicit val readString = fromStringReader[String](s => _ => Right(s))
  implicit val readBoolean = fromNonEmptyStringReader[Boolean](catchReadError(_.toBoolean))
  implicit val readDouble = fromNonEmptyStringReader[Double](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toDouble / 100d
    case v => v.toDouble
  }))
  implicit val readFloat = fromNonEmptyStringReader[Float](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toFloat / 100f
    case v => v.toFloat
  }))
  implicit val readInt = fromNonEmptyStringReader[Int](catchReadError(_.toInt))
  implicit val readLong = fromNonEmptyStringReader[Long](catchReadError(_.toLong))
  implicit val readShort = fromNonEmptyStringReader[Short](catchReadError(_.toShort))
  implicit val readURL = fromNonEmptyStringConvert[URL](catchReadError(new URL(_)), _.toString)
  implicit val readUUID = fromNonEmptyStringConvert[UUID](catchReadError(UUID.fromString), _.toString)
  implicit val readPath = fromStringConvert[Path](catchReadError(Paths.get(_)), _.toString)
  implicit val readURI = fromStringConvert[URI](catchReadError(new URI(_)), _.toString)

  implicit val readConfig: ConfigConvert[Config] = new ConfigConvert[Config] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Config] = config match {
      case co: ConfigObject => Right(co.toConfig)
      case other => fail(WrongType(other.valueType().toString, "ConfigObject", ConfigValueLocation(config)))
    }
    override def to(t: Config): ConfigValue = t.root()
  }

  implicit val readConfigObject: ConfigConvert[ConfigObject] = new ConfigConvert[ConfigObject] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigObject] = config match {
      case c: ConfigObject => Right(c)
      case other => fail(WrongType(other.valueType().toString, "ConfigObject", ConfigValueLocation(config)))
    }
    override def to(t: ConfigObject): ConfigValue = t
  }

  implicit val readConfigValue: ConfigConvert[ConfigValue] = new ConfigConvert[ConfigValue] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigValue] = Right(config)
    override def to(t: ConfigValue): ConfigValue = t
  }
  implicit val readConfigList: ConfigConvert[ConfigList] = new ConfigConvert[ConfigList] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigList] = config match {
      case c: ConfigList => Right(c)
      case other => fail(WrongType(other.valueType().toString, "ConfigList", ConfigValueLocation(config)))
    }
    override def to(t: ConfigList): ConfigValue = t
  }
}
