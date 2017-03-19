package pureconfig

import java.net.{ URI, URL }
import java.nio.file.{ Path, Paths }
import java.time._
import java.util.UUID

import scala.concurrent.duration.{ Duration, FiniteDuration }

import com.typesafe.config._
import pureconfig.error._

/**
 * Trait containing `ConfigReader` instances for primitive types.
 */
trait PrimitiveReaders extends ConvertHelpers {

  implicit val stringConfigReader = fromStringReader[String](s => _ => Right(s))
  implicit val booleanConfigReader = fromNonEmptyStringReader[Boolean](catchReadError(_.toBoolean))
  implicit val doubleConfigReader = fromNonEmptyStringReader[Double](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toDouble / 100d
    case v => v.toDouble
  }))
  implicit val floatConfigReader = fromNonEmptyStringReader[Float](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toFloat / 100f
    case v => v.toFloat
  }))
  implicit val intConfigReader = fromNonEmptyStringReader[Int](catchReadError(_.toInt))
  implicit val longConfigReader = fromNonEmptyStringReader[Long](catchReadError(_.toLong))
  implicit val shortConfigReader = fromNonEmptyStringReader[Short](catchReadError(_.toShort))
}

/**
 * Trait containing `ConfigReader` instances for classes related to file system paths and URIs.
 */
trait UriAndPathReaders extends ConvertHelpers {

  implicit val urlConfigReader = fromNonEmptyStringReader[URL](catchReadError(new URL(_)))
  implicit val uuidConfigReader = fromNonEmptyStringReader[UUID](catchReadError(UUID.fromString))
  implicit val pathConfigReader = fromStringReader[Path](catchReadError(Paths.get(_)))
  implicit val uriConfigReader = fromStringReader[URI](catchReadError(new URI(_)))
}

/**
 * Trait containing `ConfigReader` instances for `java.time` classes.
 */
trait JavaTimeReaders extends ConvertHelpers {

  implicit val instantConfigReader: ConfigReader[Instant] =
    fromNonEmptyStringReader[Instant](catchReadError(Instant.parse))

  implicit val zoneOffsetConfigReader: ConfigReader[ZoneOffset] =
    fromNonEmptyStringReader[ZoneOffset](catchReadError(ZoneOffset.of))

  implicit val zoneIdConfigReader: ConfigReader[ZoneId] =
    fromNonEmptyStringReader[ZoneId](catchReadError(ZoneId.of))

  implicit val periodConfigReader: ConfigReader[Period] =
    fromNonEmptyStringReader[Period](catchReadError(Period.parse))

  implicit val yearConfigReader: ConfigReader[Year] =
    fromNonEmptyStringReader[Year](catchReadError(Year.parse))
}

/**
 * Trait containing `ConfigReader` instances for [[Duration]] and [[FiniteDuration]].
 */
trait DurationReaders extends ConvertHelpers {

  implicit val durationConfigReader: ConfigReader[Duration] =
    fromNonEmptyStringReader[Duration](DurationConvert.fromString)

  implicit val finiteDurationConfigReader: ConfigReader[FiniteDuration] = {
    val fromString: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, FiniteDuration] = { string => location =>
      DurationConvert.fromString(string)(location).right.flatMap {
        case d: FiniteDuration => Right(d)
        case _ => Left(CannotConvert(string, "FiniteDuration", s"Couldn't parse '$string' into a FiniteDuration because it's infinite.", location, None))
      }
    }
    fromNonEmptyStringReader[FiniteDuration](fromString)
  }
}

/**
 * Trait containing `ConfigReader` instances for Typesafe config models.
 */
trait TypesafeConfigReaders extends ConvertHelpers {

  implicit val configConfigReader: ConfigReader[Config] = new ConfigReader[Config] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Config] = config match {
      case co: ConfigObject => Right(co.toConfig)
      case other => fail(WrongType(other.valueType().toString, "ConfigObject", ConfigValueLocation(config), None))
    }
  }

  implicit val configObjectConfigReader: ConfigReader[ConfigObject] = new ConfigReader[ConfigObject] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigObject] = config match {
      case c: ConfigObject => Right(c)
      case other => fail(WrongType(other.valueType().toString, "ConfigObject", ConfigValueLocation(config), None))
    }
  }

  implicit val configValueConfigReader: ConfigReader[ConfigValue] = new ConfigReader[ConfigValue] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigValue] = Right(config)
  }

  implicit val configListConfigReader: ConfigReader[ConfigList] = new ConfigReader[ConfigList] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigList] = config match {
      case c: ConfigList => Right(c)
      case other => fail(WrongType(other.valueType().toString, "ConfigList", ConfigValueLocation(config), None))
    }
  }
}

/**
 * Trait containing `ConfigReader` instances for primitive types and simple classes in Java and Scala standard
 * libraries.
 */
trait BasicReaders
  extends PrimitiveReaders
  with UriAndPathReaders
  with JavaTimeReaders
  with DurationReaders
  with TypesafeConfigReaders

object BasicReaders extends BasicReaders
