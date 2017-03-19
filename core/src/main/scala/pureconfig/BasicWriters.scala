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
trait PrimitiveWriters extends ConvertHelpers {

  implicit val stringConfigWriter = primitiveWriter[String]
  implicit val booleanConfigWriter = primitiveWriter[Boolean]
  implicit val doubleConfigWriter = primitiveWriter[Double]
  implicit val floatConfigWriter = primitiveWriter[Float]
  implicit val intConfigWriter = primitiveWriter[Int]
  implicit val longConfigWriter = primitiveWriter[Long]
  implicit val shortConfigWriter = primitiveWriter[Short]
}

/**
 * Trait containing `ConfigWriter` instances for classes related to file system paths and URIs.
 */
trait UriAndPathWriters extends ConvertHelpers {

  implicit val urlConfigWriter = fromDefaultStringWriter[URL]
  implicit val uuidConfigWriter = fromDefaultStringWriter[UUID]
  implicit val pathConfigWriter = fromDefaultStringWriter[Path]
  implicit val uriConfigWriter = fromDefaultStringWriter[URI]
}

/**
 * Trait containing `ConfigWriter` instances for `java.time` classes.
 */
trait JavaTimeWriters extends ConvertHelpers {

  implicit val instantConfigWriter = fromDefaultStringWriter[Instant]
  implicit val zoneOffsetConfigWriter = fromDefaultStringWriter[ZoneOffset]
  implicit val zoneIdConfigWriter = fromDefaultStringWriter[ZoneId]
  implicit val periodConfigWriter = fromDefaultStringWriter[Period]

  // see documentation for [[java.time.Year.parse]]
  private[this] def yearToString(year: Year): String =
    if (year.getValue > 9999) "+" + year else year.toString

  implicit val yearConfigConvert = fromStringWriter[Year](yearToString)
}

/**
 * Trait containing `ConfigWriter` instances for [[Duration]] and [[FiniteDuration]].
 */
trait DurationWriters extends ConvertHelpers {

  implicit val durationConfigWriter = fromStringWriter[Duration](DurationConvert.fromDuration)
  implicit val finiteDurationConfigWriter = fromStringWriter[FiniteDuration](DurationConvert.fromDuration)
}

/**
 * Trait containing `ConfigWriter` instances for Typesafe config models.
 */
trait TypesafeConfigWriters extends ConvertHelpers {

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
