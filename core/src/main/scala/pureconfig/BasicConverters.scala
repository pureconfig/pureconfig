package pureconfig

import java.net.{ URI, URL }
import java.nio.file.{ Path, Paths }
import java.time._
import java.util.UUID

import scala.concurrent.duration.{ Duration, FiniteDuration }

import com.typesafe.config._
import pureconfig.error._

/**
 * Trait containing `ConfigConvert` instances for primitive types and simple classes in Java and Scala standard
 * libraries.
 */
trait BasicConverters extends ConvertHelpers {

  // primitive type converters

  implicit val stringConfigConvert = fromStringReader[String](s => _ => Right(s))
  implicit val booleanConfigConvert = fromNonEmptyStringReader[Boolean](catchReadError(_.toBoolean))
  implicit val doubleConfigConvert = fromNonEmptyStringReader[Double](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toDouble / 100d
    case v => v.toDouble
  }))
  implicit val floatConfigConvert = fromNonEmptyStringReader[Float](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toFloat / 100f
    case v => v.toFloat
  }))
  implicit val intConfigConvert = fromNonEmptyStringReader[Int](catchReadError(_.toInt))
  implicit val longConfigConvert = fromNonEmptyStringReader[Long](catchReadError(_.toLong))
  implicit val shortConfigConvert = fromNonEmptyStringReader[Short](catchReadError(_.toShort))

  // URI and file system path converters

  implicit val urlConfigConvert = fromNonEmptyStringConvert[URL](catchReadError(new URL(_)), _.toString)
  implicit val uuidConfigConvert = fromNonEmptyStringConvert[UUID](catchReadError(UUID.fromString), _.toString)
  implicit val pathConfigConvert = fromStringConvert[Path](catchReadError(Paths.get(_)), _.toString)
  implicit val uriConfigConvert = fromStringConvert[URI](catchReadError(new URI(_)), _.toString)

  // `java.time` converters

  implicit val instantConfigConvert: ConfigConvert[Instant] =
    fromNonEmptyStringReader[Instant](catchReadError(Instant.parse))

  implicit val zoneOffsetConfigConvert: ConfigConvert[ZoneOffset] =
    fromNonEmptyStringReader[ZoneOffset](catchReadError(ZoneOffset.of))

  implicit val zoneIdConfigConvert: ConfigConvert[ZoneId] =
    fromNonEmptyStringReader[ZoneId](catchReadError(ZoneId.of))

  implicit val periodConfigConvert: ConfigConvert[Period] =
    fromNonEmptyStringReader[Period](catchReadError(Period.parse))

  implicit val yearConfigConvert: ConfigConvert[Year] =
    fromNonEmptyStringReader[Year](catchReadError(Year.parse))

  // `scala.concurrent.duration` converters

  implicit val durationConfigConvert: ConfigConvert[Duration] = {
    fromNonEmptyStringConvert[Duration](DurationConvert.fromString, DurationConvert.fromDuration)
  }

  implicit val finiteDurationConfigConvert: ConfigConvert[FiniteDuration] = {
    val fromString: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, FiniteDuration] = { string => location =>
      DurationConvert.fromString(string)(location).right.flatMap {
        case d: FiniteDuration => Right(d)
        case _ => Left(CannotConvert(string, "FiniteDuration", s"Couldn't parse '$string' into a FiniteDuration because it's infinite.", location, None))
      }
    }
    fromNonEmptyStringConvert[FiniteDuration](fromString, DurationConvert.fromDuration)
  }

  // `com.typesafe.config` converters

  implicit val configConfigConvert: ConfigConvert[Config] = new ConfigConvert[Config] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Config] = config match {
      case co: ConfigObject => Right(co.toConfig)
      case other => fail(WrongType(other.valueType().toString, "ConfigObject", ConfigValueLocation(config), None))
    }
    override def to(t: Config): ConfigValue = t.root()
  }

  implicit val configObjectConfigConvert: ConfigConvert[ConfigObject] = new ConfigConvert[ConfigObject] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigObject] = config match {
      case c: ConfigObject => Right(c)
      case other => fail(WrongType(other.valueType().toString, "ConfigObject", ConfigValueLocation(config), None))
    }
    override def to(t: ConfigObject): ConfigValue = t
  }

  implicit val configValueConfigConvert: ConfigConvert[ConfigValue] = new ConfigConvert[ConfigValue] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigValue] = Right(config)
    override def to(t: ConfigValue): ConfigValue = t
  }
  implicit val configListConfigConvert: ConfigConvert[ConfigList] = new ConfigConvert[ConfigList] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigList] = config match {
      case c: ConfigList => Right(c)
      case other => fail(WrongType(other.valueType().toString, "ConfigList", ConfigValueLocation(config), None))
    }
    override def to(t: ConfigList): ConfigValue = t
  }
}

object BasicConverters extends BasicConverters
