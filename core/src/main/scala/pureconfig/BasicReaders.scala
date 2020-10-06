package pureconfig

import java.io.File
import java.math.{BigInteger, BigDecimal => JavaBigDecimal}
import java.net.{URI, URL}
import java.nio.file.{Path, Paths}
import java.time._
import java.time.{Duration => JavaDuration}
import java.util.UUID
import java.util.regex.Pattern

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.math.{BigDecimal, BigInt}
import scala.reflect.ClassTag
import scala.util.matching.Regex
import com.typesafe.config._
import pureconfig.ConvertHelpers._
import pureconfig.error._

import scala.util.Try

/** Trait containing `ConfigReader` instances for primitive types.
  */
trait PrimitiveReaders {

  implicit val stringConfigReader = ConfigReader.fromCursor(_.asString)

  implicit val charConfigReader = ConfigReader.fromNonEmptyString[Char](s =>
    s.size match {
      case 1 => Right(s.charAt(0))
      case len => Left(WrongSizeString(1, len))
    }
  )

  implicit val booleanConfigReader = ConfigReader.fromCursor(_.asBoolean)

  implicit val doubleConfigReader = ConfigReader.fromCursor({ cur =>
    val asStringReader = catchReadError({
      case v if v.last == '%' => v.dropRight(1).toDouble / 100f
      case v => v.toDouble
    })

    cur.asString.right.flatMap(s => cur.scopeFailure(asStringReader(s))).left.flatMap(_ => cur.asDouble)
  })

  implicit val floatConfigReader = ConfigReader.fromCursor({ cur =>
    val asStringReader = catchReadError({
      case v if v.last == '%' => v.dropRight(1).toFloat / 100f
      case v => v.toFloat
    })

    cur.asString.right.flatMap(s => cur.scopeFailure(asStringReader(s))).left.flatMap(_ => cur.asFloat)
  })

  implicit val intConfigReader = ConfigReader.fromCursor(_.asInt)

  implicit val longConfigReader = ConfigReader.fromCursor(_.asLong)

  implicit val shortConfigReader = ConfigReader.fromCursor(_.asShort)

  implicit val byteConfigReader = ConfigReader.fromCursor(_.asByte)
}

/** Trait containing `ConfigReader` instance for Java Enums.
  */
trait JavaEnumReader {

  implicit def javaEnumReader[A <: Enum[A]](implicit tag: ClassTag[A]): ConfigReader[A] =
    ConfigReader.fromString(catchReadError(s => {
      val enumClass = tag.runtimeClass.asInstanceOf[Class[A]]
      Enum.valueOf(enumClass, s)
    }))
}

/** Trait containing `ConfigReader` instances for classes related to file system paths and URIs.
  */
trait UriAndPathReaders {

  implicit val urlConfigReader = ConfigReader.fromNonEmptyString[URL](catchReadError(new URL(_)))
  implicit val uuidConfigReader = ConfigReader.fromNonEmptyString[UUID](catchReadError(UUID.fromString))
  implicit val pathConfigReader = ConfigReader.fromString[Path](catchReadError(Paths.get(_)))
  implicit val fileConfigReader = ConfigReader.fromString[File](catchReadError(new File(_)))
  implicit val uriConfigReader = ConfigReader.fromString[URI](catchReadError(new URI(_)))
}

/** Trait containing `ConfigReader` instances for classes related to regular expressions.
  */
trait RegexReaders {

  implicit val patternReader = ConfigReader.fromString[Pattern](catchReadError(Pattern.compile))
  implicit val regexReader = ConfigReader.fromString[Regex](catchReadError(new Regex(_)))
}

/** Trait containing `ConfigReader` instances for `java.time` classes.
  */
trait JavaTimeReaders {

  implicit val instantConfigReader: ConfigReader[Instant] =
    ConfigReader.fromNonEmptyString[Instant](catchReadError(Instant.parse))

  implicit val zoneOffsetConfigReader: ConfigReader[ZoneOffset] =
    ConfigReader.fromNonEmptyString[ZoneOffset](catchReadError(ZoneOffset.of))

  implicit val zoneIdConfigReader: ConfigReader[ZoneId] =
    ConfigReader.fromNonEmptyString[ZoneId](catchReadError(ZoneId.of))

  implicit val periodConfigReader: ConfigReader[Period] =
    ConfigReader.fromNonEmptyString[Period](PeriodUtils.fromString)

  implicit val javaDurationConfigReader: ConfigReader[JavaDuration] =
    ConfigReader.fromNonEmptyString[JavaDuration](catchReadError(JavaDuration.parse))

  implicit val yearConfigReader: ConfigReader[Year] =
    ConfigReader.fromNonEmptyString[Year](catchReadError(Year.parse))
}

/** Trait containing `ConfigReader` instances for [[scala.concurrent.duration.Duration]] and
  * [[scala.concurrent.duration.FiniteDuration]].
  */
trait DurationReaders {

  implicit val durationConfigReader: ConfigReader[Duration] =
    ConfigReader.fromNonEmptyString[Duration](DurationUtils.fromString)

  implicit val finiteDurationConfigReader: ConfigReader[FiniteDuration] = {
    val fromString: String => Either[FailureReason, FiniteDuration] = { string =>
      DurationUtils.fromString(string).right.flatMap {
        case d: FiniteDuration => Right(d)
        case _ =>
          Left(
            CannotConvert(
              string,
              "FiniteDuration",
              s"Couldn't parse '$string' into a FiniteDuration because it's infinite."
            )
          )
      }
    }
    ConfigReader.fromNonEmptyString[FiniteDuration](fromString)
  }
}

/** Trait containing `ConfigReader` instances for Java and Scala arbitrary-precision numeric types.
  */
trait NumericReaders {

  implicit val javaBigIntegerReader: ConfigReader[BigInteger] =
    ConfigReader.fromNonEmptyString[BigInteger](catchReadError(new BigInteger(_)))

  implicit val javaBigDecimalReader: ConfigReader[JavaBigDecimal] =
    ConfigReader.fromNonEmptyString[JavaBigDecimal](catchReadError(new JavaBigDecimal(_)))

  implicit val scalaBigIntReader: ConfigReader[BigInt] =
    ConfigReader.fromNonEmptyString[BigInt](catchReadError(BigInt(_)))

  implicit val scalaBigDecimalReader: ConfigReader[BigDecimal] =
    ConfigReader.fromNonEmptyString[BigDecimal](catchReadError(BigDecimal(_)))
}

/** Trait containing `ConfigReader` instances for Typesafe config models.
  */
trait TypesafeConfigReaders {

  implicit val configConfigReader: ConfigReader[Config] =
    ConfigReader.fromCursor(_.asObjectCursor.right.map(_.objValue.toConfig))

  implicit val configObjectConfigReader: ConfigReader[ConfigObject] =
    ConfigReader.fromCursor(_.asObjectCursor.right.map(_.objValue))

  implicit val configValueConfigReader: ConfigReader[ConfigValue] =
    ConfigReader.fromCursor(_.asConfigValue)

  implicit val configListConfigReader: ConfigReader[ConfigList] =
    ConfigReader.fromCursor(_.asListCursor.right.map(_.listValue))

  // TODO: improve error handling by copying the parsing logic from Typesafe and making it use PureConfig Results
  // (see PeriodUtils and DurationUtils)
  implicit val configMemorySizeReader: ConfigReader[ConfigMemorySize] =
    ConfigReader.fromCursor { cur =>
      cur.scopeFailure {
        ConvertHelpers.tryToEither {
          Try {
            val wrapped = cur.asConfigValue.right.get.atKey("_")
            val bytes = wrapped.getBytes("_").longValue()
            ConfigMemorySize.ofBytes(bytes)
          }
        }
      }
    }
}

/** Trait containing `ConfigReader` instances for primitive types and simple classes in Java and Scala standard
  * libraries.
  */
trait BasicReaders
    extends PrimitiveReaders
    with JavaEnumReader
    with UriAndPathReaders
    with RegexReaders
    with JavaTimeReaders
    with DurationReaders
    with NumericReaders
    with TypesafeConfigReaders

object BasicReaders extends BasicReaders
