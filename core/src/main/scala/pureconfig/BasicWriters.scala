package pureconfig

import java.net.{ URI, URL }
import java.nio.file.Path
import java.time._
import java.util.UUID

import scala.concurrent.duration.{ Duration, FiniteDuration }

import com.typesafe.config._

/**
 * Trait containing `ConfigWriter` instances for primitive types.
 */
trait PrimitiveWriters {

  implicit val stringConfigWriter = ConfigWriter.forPrimitive[String]
  implicit val booleanConfigWriter = ConfigWriter.forPrimitive[Boolean]
  implicit val doubleConfigWriter = ConfigWriter.forPrimitive[Double]
  implicit val floatConfigWriter = ConfigWriter.forPrimitive[Float]
  implicit val intConfigWriter = ConfigWriter.forPrimitive[Int]
  implicit val longConfigWriter = ConfigWriter.forPrimitive[Long]
  implicit val shortConfigWriter = ConfigWriter.forPrimitive[Short]
}

/**
 * Trait containing `ConfigWriter` instances for classes related to file system paths and URIs.
 */
trait UriAndPathWriters {

  implicit val urlConfigWriter = ConfigWriter.toDefaultString[URL]
  implicit val uuidConfigWriter = ConfigWriter.toDefaultString[UUID]
  implicit val pathConfigWriter = ConfigWriter.toDefaultString[Path]
  implicit val uriConfigWriter = ConfigWriter.toDefaultString[URI]
}

/**
 * Trait containing `ConfigWriter` instances for `java.time` classes.
 */
trait JavaTimeWriters {

  implicit val instantConfigWriter = ConfigWriter.toDefaultString[Instant]
  implicit val zoneOffsetConfigWriter = ConfigWriter.toDefaultString[ZoneOffset]
  implicit val zoneIdConfigWriter = ConfigWriter.toDefaultString[ZoneId]
  implicit val periodConfigWriter = ConfigWriter.toDefaultString[Period]

  // see documentation for [[java.time.Year.parse]]
  private[this] def yearToString(year: Year): String =
    if (year.getValue > 9999) "+" + year else year.toString

  implicit val yearConfigWriter = ConfigWriter.toString[Year](yearToString)
}

/**
 * Trait containing `ConfigWriter` instances for [[Duration]] and [[FiniteDuration]].
 */
trait DurationWriters {

  implicit val durationConfigWriter = ConfigWriter.toString[Duration](DurationConvert.fromDuration)
  implicit val finiteDurationConfigWriter = ConfigWriter.toString[FiniteDuration](DurationConvert.fromDuration)
}

/**
 * Trait containing `ConfigWriter` instances for Typesafe config models.
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
}

/**
 * Trait containing `ConfigWriter` instances for primitive types and simple classes in Java and Scala standard
 * libraries.
 */
trait BasicWriters
  extends PrimitiveWriters
  with UriAndPathWriters
  with JavaTimeWriters
  with DurationWriters
  with TypesafeConfigWriters

object BasicWriters extends BasicWriters
