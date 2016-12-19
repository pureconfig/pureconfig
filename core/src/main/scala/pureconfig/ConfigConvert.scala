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
import java.net.URL
import scala.concurrent.duration.{ Duration, FiniteDuration }

import pureconfig.ConfigConvert.{ fromNonEmptyString, fromString, stringConvert, nonEmptyStringConvert }
import pureconfig.error.{ CannotConvertNullException, KeyNotFoundException, WrongTypeException, WrongTypeForKeyException }

/**
 * Trait for conversion between `T` and `ConfigValue`.
 */
trait ConfigConvert[T] {
  /**
   * Convert the given configuration into an instance of `T` if possible.
   *
   * @param config The configuration from which load the config
   * @return `Success` of `T` if the conversion is possible, `Failure` with the problem if the
   *         conversion is not
   */
  def from(config: ConfigValue): Try[T]

  /**
   * Converts a type `T` to a `ConfigValue`.
   *
   * @param t The instance of `T` to convert
   * @return The `ConfigValue` obtained from the `T` instance
   */
  def to(t: T): ConfigValue
}

object ConfigConvert extends LowPriorityConfigConvertImplicits {
  def apply[T](implicit conv: ConfigConvert[T]): ConfigConvert[T] = conv

  private def fromFConvert[T](fromF: String => Try[T]): ConfigValue => Try[T] =
    config => {
      // Because we can't trust `fromF` or Typesafe Config not to throw, we wrap the
      // evaluation in one more `Try` to prevent an unintentional exception from escaping.
      // `Try.flatMap(f)` captures any non-fatal exceptions thrown by `f`.
      Try(config.valueType match {
        case ConfigValueType.STRING => config.unwrapped.toString
        case _ => config.render(ConfigRenderOptions.concise)
      }).flatMap(fromF)
    }

  def fromString[T](fromF: String => Try[T]): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Try[T] = fromFConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  def fromNonEmptyString[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromString(ensureNonEmpty(ct)(_).flatMap(fromF))
  }

  private def ensureNonEmpty[T](implicit ct: ClassTag[T]): String => Try[String] = {
    case "" => Failure(new IllegalArgumentException(s"Cannot read a $ct from an empty string."))
    case x => Success(x)
  }

  def stringConvert[T](fromF: String => Try[T], toF: T => String): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Try[T] = fromFConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(toF(t))
  }

  def nonEmptyStringConvert[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    stringConvert(ensureNonEmpty(ct)(_).flatMap(fromF), toF)
  }

  private[pureconfig] trait WrappedDefaultValueConfigConvert[Wrapped, SubRepr <: HList, DefaultRepr <: HList] extends ConfigConvert[SubRepr] {
    final def from(config: ConfigValue): Try[SubRepr] =
      Failure(
        new UnsupportedOperationException("Cannot call 'from' on a WrappedDefaultValueConfigConvert."))
    def fromWithDefault(config: ConfigValue, default: DefaultRepr): Try[SubRepr] = config match {
      case co: ConfigObject =>
        fromConfigObject(co, default)
      case null =>
        Failure(CannotConvertNullException)
      case other =>
        Failure(WrongTypeException(config.valueType().toString))
    }
    def fromConfigObject(co: ConfigObject, default: DefaultRepr): Try[SubRepr]
    def to(v: SubRepr): ConfigValue
  }

  implicit def hNilConfigConvert[Wrapped]: WrappedDefaultValueConfigConvert[Wrapped, HNil, HNil] = new WrappedDefaultValueConfigConvert[Wrapped, HNil, HNil] {
    override def fromConfigObject(config: ConfigObject, default: HNil): Try[HNil] = Success(HNil)
    override def to(t: HNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  private[pureconfig] def improveFailure[Z](result: Try[Z], keyStr: String): Try[Z] =
    result recoverWith {
      case CannotConvertNullException => Failure(KeyNotFoundException(keyStr))
      case KeyNotFoundException(suffix) => Failure(KeyNotFoundException(keyStr + "." + suffix))
      case WrongTypeException(typ) => Failure(WrongTypeForKeyException(typ, keyStr))
      case WrongTypeForKeyException(typ, suffix) => Failure(WrongTypeForKeyException(typ, keyStr + "." + suffix))
    }

  implicit def hConsConfigConvert[Wrapped, K <: Symbol, V, T <: HList, U <: HList](
    implicit key: Witness.Aux[K],
    vFieldConvert: Lazy[ConfigConvert[V]],
    tConfigConvert: Lazy[WrappedDefaultValueConfigConvert[Wrapped, T, U]],
    mapping: ConfigFieldMapping[Wrapped]): WrappedDefaultValueConfigConvert[Wrapped, FieldType[K, V] :: T, Option[V] :: U] = new WrappedDefaultValueConfigConvert[Wrapped, FieldType[K, V]:: T, Option[V]:: U] {

    override def fromConfigObject(co: ConfigObject, default: Option[V] :: U): Try[FieldType[K, V] :: T] = {
      val keyStr = mapping(key.value.toString().tail)
      for {
        v <- improveFailure[V](
          (co.get(keyStr), vFieldConvert.value) match {
            case (null, converter: OptionConfigConvert[_]) =>
              converter.from(co.get(keyStr))
            case (null, _) =>
              default.head.fold[Try[V]](Failure(CannotConvertNullException))(Success(_))
            case (value, converter) =>
              converter.from(value)
          },
          keyStr)
        tail <- tConfigConvert.value.fromWithDefault(co, default.tail)
      } yield field[K](v) :: tail
    }

    override def to(t: FieldType[K, V] :: T): ConfigValue = {
      val keyStr = mapping(key.value.toString().tail)
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

  case class NoValidCoproductChoiceFound(config: ConfigValue)
    extends RuntimeException(s"No valid coproduct type choice found for configuration $config.")

  implicit def cNilConfigConvert: ConfigConvert[CNil] = new ConfigConvert[CNil] {
    override def from(config: ConfigValue): Try[CNil] =
      Failure(NoValidCoproductChoiceFound(config))

    override def to(t: CNil): ConfigValue = ConfigFactory.parseMap(Map().asJava).root()
  }

  implicit def coproductConfigConvert[V, T <: Coproduct](
    implicit vFieldConvert: Lazy[ConfigConvert[V]],
    tConfigConvert: Lazy[ConfigConvert[T]]): ConfigConvert[V :+: T] =
    new ConfigConvert[V :+: T] {

      override def from(config: ConfigValue): Try[V :+: T] = {
        vFieldConvert.value.from(config)
          .map(s => Inl[V, T](s))
          .orElse(tConfigConvert.value.from(config).map(s => Inr[V, T](s)))
      }

      override def to(t: V :+: T): ConfigValue = t match {
        case Inl(l) => vFieldConvert.value.to(l)
        case Inr(r) => tConfigConvert.value.to(r)
      }
    }

  // For Option[T] we use a special config converter
  implicit def deriveOption[T](implicit conv: Lazy[ConfigConvert[T]]) = new OptionConfigConvert[T]

  class OptionConfigConvert[T](implicit conv: Lazy[ConfigConvert[T]]) extends ConfigConvert[Option[T]] {
    override def from(config: ConfigValue): Try[Option[T]] = {
      if (config == null || config.unwrapped() == null)
        Success(None)
      else
        conv.value.from(config).map(Some(_))
    }

    override def to(t: Option[T]): ConfigValue = t match {
      case Some(v) => conv.value.to(v)
      case None => ConfigValueFactory.fromMap(Map().asJava)
    }

    def toOption(t: Option[T]): Option[ConfigValue] = t.map(conv.value.to)
  }

  // traversable of types with an instance of ConfigConvert
  implicit def deriveTraversable[T, F[T] <: TraversableOnce[T]](implicit configConvert: Lazy[ConfigConvert[T]],
    cbf: CanBuildFrom[F[T], T, F[T]]) = new ConfigConvert[F[T]] {

    override def from(config: ConfigValue): Try[F[T]] = {
      config match {
        case co: ConfigList =>
          val tryBuilder = co.asScala.foldLeft(Try(cbf())) {
            case (tryResult, v) =>
              for {
                result <- tryResult
                value <- configConvert.value.from(v)
              } yield result += value
          }

          tryBuilder.map(_.result())
        case null =>
          Failure(CannotConvertNullException)
        case other =>
          Failure(WrongTypeException(other.valueType().toString))
      }
    }

    override def to(ts: F[T]): ConfigValue = {
      ConfigValueFactory.fromIterable(ts.toList.map(configConvert.value.to).asJava)
    }
  }

  implicit def deriveMap[T](implicit configConvert: Lazy[ConfigConvert[T]]) = new ConfigConvert[Map[String, T]] {

    override def from(config: ConfigValue): Try[Map[String, T]] = {
      config match {
        case co: ConfigObject =>
          val keysFound = co.keySet().asScala.toList

          keysFound.foldLeft(Try(Map.empty[String, T])) {
            case (f @ Failure(_), _) => f
            case (Success(acc), key) =>
              for {
                rawValue <- Try(co.get(key))
                value <- configConvert.value.from(rawValue)
              } yield acc + (key -> value)
          }
        case null =>
          Failure(CannotConvertNullException)
        case other =>
          Failure(WrongTypeException(other.valueType().toString))
      }
    }

    override def to(keyVals: Map[String, T]): ConfigValue = {
      ConfigValueFactory.fromMap(keyVals.mapValues(configConvert.value.to).asJava)
    }
  }

  // used for products
  implicit def deriveInstanceWithLabelledGeneric[F, Repr <: HList, DefaultRepr <: HList](
    implicit gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cc: Lazy[WrappedDefaultValueConfigConvert[F, Repr, DefaultRepr]]): ConfigConvert[F] = new ConfigConvert[F] {

    override def from(config: ConfigValue): Try[F] = {
      cc.value.fromWithDefault(config, default()).map(gen.from)
    }

    override def to(t: F): ConfigValue = {
      cc.value.to(gen.to(t))
    }
  }

  // used for coproducts
  implicit def deriveInstanceWithGeneric[F, Repr <: Coproduct](
    implicit gen: Generic.Aux[F, Repr],
    cc: Lazy[ConfigConvert[Repr]]): ConfigConvert[F] = new ConfigConvert[F] {
    override def from(config: ConfigValue): Try[F] = {
      cc.value.from(config).map(gen.from)
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
    nonEmptyStringConvert(
      s => DurationConvert.fromString(s, implicitly[ClassTag[Duration]]),
      DurationConvert.fromDuration
    )
  }

  implicit val finiteDurationConfigConvert: ConfigConvert[FiniteDuration] = {
    val fromString: String => Try[FiniteDuration] = { (s: String) =>
      DurationConvert.fromString(s, implicitly[ClassTag[FiniteDuration]])
        .flatMap {
          case d: FiniteDuration => Success(d)
          case _ => Failure(new IllegalArgumentException(s"Couldn't parse '$s' into a FiniteDuration because it's infinite."))
        }
    }
    nonEmptyStringConvert(fromString, DurationConvert.fromDuration)
  }

  implicit val readString = fromString[String](Success(_))
  implicit val readBoolean = fromNonEmptyString[Boolean](s => Try(s.toBoolean))
  implicit val readDouble = fromNonEmptyString[Double]({
    case v if v.last == '%' => Try(v.dropRight(1).toDouble / 100d)
    case v => Try(v.toDouble)
  })
  implicit val readFloat = fromNonEmptyString[Float]({
    case v if v.last == '%' => Try(v.dropRight(1).toFloat / 100f)
    case v => Try(v.toFloat)
  })
  implicit val readInt = fromNonEmptyString[Int](s => Try(s.toInt))
  implicit val readLong = fromNonEmptyString[Long](s => Try(s.toLong))
  implicit val readShort = fromNonEmptyString[Short](s => Try(s.toShort))
  implicit val readURL = stringConvert[URL](s => Try(new URL(s)), _.toString)

  implicit val readConfig: ConfigConvert[Config] = new ConfigConvert[Config] {
    override def from(config: ConfigValue): Try[Config] = config match {
      case co: ConfigObject => Success(co.toConfig)
      case null => Failure(CannotConvertNullException)
      case other => Failure(WrongTypeException(other.valueType().toString))
    }
    override def to(t: Config): ConfigValue = t.root()
  }

  implicit val readConfigObject: ConfigConvert[ConfigObject] = new ConfigConvert[ConfigObject] {
    override def from(config: ConfigValue): Try[ConfigObject] = config match {
      case c: ConfigObject => Success(c)
      case null => Failure(CannotConvertNullException)
      case other => Failure(WrongTypeException(other.valueType().toString))
    }
    override def to(t: ConfigObject): ConfigValue = t
  }

  implicit val readConfigValue: ConfigConvert[ConfigValue] = new ConfigConvert[ConfigValue] {
    override def from(config: ConfigValue): Try[ConfigValue] = Success(config)
    override def to(t: ConfigValue): ConfigValue = t
  }

  implicit val readConfigList: ConfigConvert[ConfigList] = new ConfigConvert[ConfigList] {
    override def from(config: ConfigValue): Try[ConfigList] = config match {
      case c: ConfigList => Success(c)
      case null => Failure(CannotConvertNullException)
      case other => Failure(WrongTypeException(other.valueType().toString))
    }
    override def to(t: ConfigList): ConfigValue = t
  }
}
