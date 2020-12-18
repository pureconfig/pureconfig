package pureconfig

import java.io.File
import java.math.{BigDecimal => JavaBigDecimal, BigInteger}
import java.net.{URI, URL}
import java.nio.file.Path
import java.time._
import java.time.{Duration => JavaDuration}
import java.util.UUID
import java.util.regex.Pattern

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.math.{BigDecimal, BigInt}
import scala.util.matching.Regex

import com.typesafe.config._

/** Trait containing `ConfigWriter` instances for primitive types.
  */
trait PrimitiveWriters {

  implicit val stringConfigWriter: ConfigWriter[String] = ConfigWriter.forPrimitive[String]
  implicit val charConfigWriter: ConfigWriter[Char] = ConfigWriter.toDefaultString[Char]
  implicit val booleanConfigWriter: ConfigWriter[Boolean] = ConfigWriter.forPrimitive[Boolean]
  implicit val doubleConfigWriter: ConfigWriter[Double] = ConfigWriter.forPrimitive[Double]
  implicit val floatConfigWriter: ConfigWriter[Float] = ConfigWriter.forPrimitive[Float]
  implicit val intConfigWriter: ConfigWriter[Int] = ConfigWriter.forPrimitive[Int]
  implicit val longConfigWriter: ConfigWriter[Long] = ConfigWriter.forPrimitive[Long]
  implicit val shortConfigWriter: ConfigWriter[Short] = ConfigWriter.forPrimitive[Short]
  implicit val byteConfigWriter: ConfigWriter[Byte] = ConfigWriter.forPrimitive[Byte]
}

/** Trait containing instance for `ConfigWriter` for Java Enum.
  */
trait JavaEnumWriter {

  implicit def javaEnumWriter[A <: java.lang.Enum[A]]: ConfigWriter[A] = ConfigWriter.toDefaultString[A]
}

/** Trait containing `ConfigWriter` instances for classes related to file system paths and URIs.
  */
trait UriAndPathWriters {

  implicit val urlConfigWriter: ConfigWriter[URL] = ConfigWriter.toDefaultString[URL]
  implicit val uuidConfigWriter: ConfigWriter[UUID] = ConfigWriter.toDefaultString[UUID]
  implicit val pathConfigWriter: ConfigWriter[Path] = ConfigWriter.toDefaultString[Path]
  implicit val fileConfigWriter: ConfigWriter[File] = ConfigWriter.toDefaultString[File]
  implicit val uriConfigWriter: ConfigWriter[URI] = ConfigWriter.toDefaultString[URI]
}

/** Trait containing `ConfigWriter` instances for classes related to regular expressions.
  */
trait RegexWriters {

  implicit val patternWriter: ConfigWriter[Pattern] = ConfigWriter.toString[Pattern](_.pattern)
  implicit val regexWriter: ConfigWriter[Regex] =
    ConfigWriter.toString[Regex](_.pattern.pattern) // Regex.regex isn't supported until 2.11
}

/** Trait containing `ConfigWriter` instances for `java.time` classes.
  */
trait JavaTimeWriters {

  implicit val instantConfigWriter: ConfigWriter[Instant] = ConfigWriter.toDefaultString[Instant]
  implicit val zoneOffsetConfigWriter: ConfigWriter[ZoneOffset] = ConfigWriter.toDefaultString[ZoneOffset]
  implicit val zoneIdConfigWriter: ConfigWriter[ZoneId] = ConfigWriter.toDefaultString[ZoneId]
  implicit val periodConfigWriter: ConfigWriter[Period] = ConfigWriter.toDefaultString[Period]

  // see documentation for [[java.time.Year.parse]]
  private[this] def yearToString(year: Year): String =
    if (year.getValue > 9999) "+" + year else year.toString

  implicit val yearConfigWriter: ConfigWriter[Year] = ConfigWriter.toString[Year](yearToString)
  implicit val javaDurationConfigWriter: ConfigWriter[JavaDuration] = ConfigWriter.toDefaultString[JavaDuration]
}

/** Trait containing `ConfigWriter` instances for [[scala.concurrent.duration.Duration]] and
  * [[scala.concurrent.duration.FiniteDuration]].
  */
trait DurationWriters {

  implicit val durationConfigWriter: ConfigWriter[Duration] =
    ConfigWriter.toString[Duration](DurationUtils.fromDuration)
  implicit val finiteDurationConfigWriter: ConfigWriter[FiniteDuration] =
    ConfigWriter.toString[FiniteDuration](DurationUtils.fromDuration)
}

/** Trait containing `ConfigWriter` instances for Java and Scala arbitrary-precision numeric types.
  */
trait NumericWriters {

  implicit val javaBigDecimalWriter: ConfigWriter[JavaBigDecimal] = ConfigWriter.toDefaultString[JavaBigDecimal]
  implicit val bigIntegerWriter: ConfigWriter[BigInteger] = ConfigWriter.toDefaultString[BigInteger]
  implicit val scalaBigDecimalWriter: ConfigWriter[BigDecimal] = ConfigWriter.toDefaultString[BigDecimal]
  implicit val scalaBigIntWriter: ConfigWriter[BigInt] = ConfigWriter.toDefaultString[BigInt]
}

/** Trait containing `ConfigWriter` instances for Typesafe config models.
  */
trait TypesafeConfigWriters {

  implicit val configConfigWriter: ConfigWriter[Config] = new ConfigWriter[Config] {
    def to(t: Config) = t.root()
  }

  implicit val configObjectConfigWriter: ConfigWriter[ConfigObject] = new ConfigWriter[ConfigObject] {
    def to(t: ConfigObject) = t
  }

  implicit val configValueConfigWriter: ConfigWriter[ConfigValue] = new ConfigWriter[ConfigValue] {
    def to(t: ConfigValue) = t
  }

  implicit val configListConfigWriter: ConfigWriter[ConfigList] = new ConfigWriter[ConfigList] {
    def to(t: ConfigList) = t
  }

  implicit val configMemorySizeWriter: ConfigWriter[ConfigMemorySize] = {
    ConfigWriter.longConfigWriter.contramap(_.toBytes)
  }
}

/** Trait containing `ConfigWriter` instances for primitive types and simple classes in Java and Scala standard
  * libraries.
  */
trait BasicWriters
    extends PrimitiveWriters
    with JavaEnumWriter
    with UriAndPathWriters
    with RegexWriters
    with JavaTimeWriters
    with DurationWriters
    with NumericWriters
    with TypesafeConfigWriters

object BasicWriters extends BasicWriters
