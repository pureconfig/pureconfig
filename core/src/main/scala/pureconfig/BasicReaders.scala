package pureconfig

import java.io.File
import java.net.{ URI, URL }
import java.nio.file.{ Path, Paths }
import java.time._
import java.time.{ Duration => JavaDuration }
import java.util.UUID
import java.util.regex.Pattern

import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.reflect.ClassTag
import scala.util.matching.Regex

import com.typesafe.config._
import pureconfig.ConvertHelpers._
import pureconfig.error._

/**
 * Trait containing `ConfigReader` instances for primitive types.
 */
trait PrimitiveReaders {

  implicit val stringConfigReader = ConfigReader.fromString[String](s => _ => Right(s))
  implicit val booleanConfigReader = ConfigReader.fromNonEmptyString[Boolean](catchReadError(_.toBoolean))
  implicit val doubleConfigReader = ConfigReader.fromNonEmptyString[Double](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toDouble / 100d
    case v => v.toDouble
  }))
  implicit val floatConfigReader = ConfigReader.fromNonEmptyString[Float](catchReadError({
    case v if v.last == '%' => v.dropRight(1).toFloat / 100f
    case v => v.toFloat
  }))
  implicit val intConfigReader = ConfigReader.fromNonEmptyString[Int](catchReadError(_.toInt))
  implicit val longConfigReader = ConfigReader.fromNonEmptyString[Long](catchReadError(_.toLong))
  implicit val shortConfigReader = ConfigReader.fromNonEmptyString[Short](catchReadError(_.toShort))
}

/**
 * Trait containing `ConfigReader` instance for Java Enums.
 */
trait JavaEnumReader {

  implicit def javaEnumReader[T <: Enum[T]](implicit tag: ClassTag[T]): ConfigReader[T] =
    ConfigReader.fromString(catchReadError(s => {
      val enumClass = tag.runtimeClass.asInstanceOf[Class[T]]
      Enum.valueOf(enumClass, s)
    }))
}

/**
 * Trait containing `ConfigReader` instances for classes related to file system paths and URIs.
 */
trait UriAndPathReaders {

  implicit val urlConfigReader = ConfigReader.fromNonEmptyString[URL](catchReadError(new URL(_)))
  implicit val uuidConfigReader = ConfigReader.fromNonEmptyString[UUID](catchReadError(UUID.fromString))
  implicit val pathConfigReader = ConfigReader.fromString[Path](catchReadError(Paths.get(_)))
  implicit val fileConfigReader = ConfigReader.fromString[File](catchReadError(new File(_)))
  implicit val uriConfigReader = ConfigReader.fromString[URI](catchReadError(new URI(_)))
}

/**
 * Trait containing `ConfigReader` instances for classes related to regular expressions.
 */
trait RegexReaders {

  implicit val patternReader = ConfigReader.fromString[Pattern](catchReadError(Pattern.compile))
  implicit val regexReader = ConfigReader.fromString[Regex](catchReadError(new Regex(_)))
}

/**
 * Trait containing `ConfigReader` instances for `java.time` classes.
 */
trait JavaTimeReaders {

  implicit val instantConfigReader: ConfigReader[Instant] =
    ConfigReader.fromNonEmptyString[Instant](catchReadError(Instant.parse))

  implicit val zoneOffsetConfigReader: ConfigReader[ZoneOffset] =
    ConfigReader.fromNonEmptyString[ZoneOffset](catchReadError(ZoneOffset.of))

  implicit val zoneIdConfigReader: ConfigReader[ZoneId] =
    ConfigReader.fromNonEmptyString[ZoneId](catchReadError(ZoneId.of))

  implicit val periodConfigReader: ConfigReader[Period] =
    ConfigReader.fromNonEmptyString[Period](catchReadError(Period.parse))

  implicit val javaDurationConfigReader: ConfigReader[JavaDuration] =
    ConfigReader.fromNonEmptyString[JavaDuration](catchReadError(JavaDuration.parse))

  implicit val yearConfigReader: ConfigReader[Year] =
    ConfigReader.fromNonEmptyString[Year](catchReadError(Year.parse))
}

/**
 * Trait containing `ConfigReader` instances for [[scala.concurrent.duration.Duration]] and
 * [[scala.concurrent.duration.FiniteDuration]].
 */
trait DurationReaders {

  implicit val durationConfigReader: ConfigReader[Duration] =
    ConfigReader.fromNonEmptyString[Duration](DurationConvert.fromString)

  implicit val finiteDurationConfigReader: ConfigReader[FiniteDuration] = {
    val fromString: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, FiniteDuration] = { string => location =>
      DurationConvert.fromString(string)(location).right.flatMap {
        case d: FiniteDuration => Right(d)
        case _ => Left(CannotConvert(string, "FiniteDuration", s"Couldn't parse '$string' into a FiniteDuration because it's infinite.", location, ""))
      }
    }
    ConfigReader.fromNonEmptyString[FiniteDuration](fromString)
  }
}

/**
 * Trait containing `ConfigReader` instances for Typesafe config models.
 */
trait TypesafeConfigReaders {

  implicit val configConfigReader: ConfigReader[Config] = new ConfigReader[Config] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Config] = config match {
      case co: ConfigObject => Right(co.toConfig)
      case other => fail(WrongType(other.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(config), ""))
    }
  }

  implicit val configObjectConfigReader: ConfigReader[ConfigObject] = new ConfigReader[ConfigObject] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigObject] = config match {
      case c: ConfigObject => Right(c)
      case other => fail(WrongType(other.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(config), ""))
    }
  }

  implicit val configValueConfigReader: ConfigReader[ConfigValue] = new ConfigReader[ConfigValue] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigValue] = Right(config)
  }

  implicit val configListConfigReader: ConfigReader[ConfigList] = new ConfigReader[ConfigList] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, ConfigList] = config match {
      case c: ConfigList => Right(c)
      case other => fail(WrongType(other.valueType, Set(ConfigValueType.LIST), ConfigValueLocation(config), ""))
    }
  }
}

/**
 * Trait containing `ConfigReader` instances for primitive types and simple classes in Java and Scala standard
 * libraries.
 */
trait BasicReaders
  extends PrimitiveReaders
  with JavaEnumReader
  with UriAndPathReaders
  with RegexReaders
  with JavaTimeReaders
  with DurationReaders
  with TypesafeConfigReaders

object BasicReaders extends BasicReaders
