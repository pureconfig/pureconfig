package pureconfig

import java.io.File
import java.math.{ BigInteger, BigDecimal => JavaBigDecimal }
import java.net.{ URI, URL }
import java.nio.file.Path
import java.time._
import java.time.{ Duration => JavaDuration }
import java.util.UUID
import java.util.regex.Pattern

import com.typesafe.config._
import pureconfig.arbitrary._
import pureconfig.data.Percentage
import pureconfig.equality._
import pureconfig.error._
import pureconfig.generic.auto._
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration.{ Duration, FiniteDuration, _ }
import scala.util.matching.Regex

class BasicConvertersSuite extends BaseSuite {
  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  behavior of "ConfigConvert"

  checkArbitrary[Duration]
  checkFailure[Duration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailures[Duration](
    ConfigValueFactory.fromIterable(List(1).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.STRING)), emptyConfigOrigin, "")))

  checkArbitrary[JavaDuration]
  checkFailure[JavaDuration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailures[JavaDuration](
    ConfigValueFactory.fromIterable(List(1).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.STRING)), emptyConfigOrigin, "")))

  checkArbitrary[FiniteDuration]
  checkFailure[FiniteDuration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailures[FiniteDuration](
    ConfigValueFactory.fromIterable(List(1).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.STRING)), emptyConfigOrigin, "")))
  checkFailure[FiniteDuration, CannotConvert](
    ConfigConvert[Duration].to(Duration.MinusInf),
    ConfigConvert[Duration].to(Duration.Inf))

  checkReadString[FiniteDuration](
    "5 seconds" -> 5.seconds,
    "28 ms" -> 28.millis,
    "28ms" -> 28.millis,
    "28" -> 28.millis,
    "28 milliseconds" -> 28.millis,
    "1d" -> 1.day)

  checkRead[FiniteDuration](
    ConfigValueFactory.fromAnyRef(28) -> 28.millis)

  checkArbitrary[Instant]

  checkArbitrary[ZoneOffset]

  checkArbitrary[ZoneId]

  checkArbitrary[Period]

  checkReadString[Period](
    "1d" -> Period.ofDays(1),
    "42" -> Period.ofDays(42),
    "4 weeks" -> Period.ofWeeks(4),
    "13 months" -> Period.ofMonths(13),
    "2y" -> Period.ofYears(2))

  checkRead[Period](
    ConfigValueFactory.fromAnyRef(42) -> Period.ofDays(42))

  checkFailure[Period, CannotConvert](
    ConfigValueFactory.fromAnyRef("4kb"),
    ConfigValueFactory.fromAnyRef("x weeks"))

  checkArbitrary[Year]

  checkArbitrary[String]

  checkArbitrary[Char]
  checkFailure[Char, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[Char, WrongSizeString](ConfigValueFactory.fromAnyRef("not a char"))

  checkArbitrary[Boolean]
  checkRead[Boolean](
    ConfigValueFactory.fromAnyRef("yes") -> true,
    ConfigValueFactory.fromAnyRef("on") -> true,
    ConfigValueFactory.fromAnyRef("no") -> false,
    ConfigValueFactory.fromAnyRef("off") -> false)

  checkArbitrary[Double]
  checkArbitrary2[Double, Percentage](_.toDoubleFraction)
  checkFailures[Double](
    ConfigValueFactory.fromAnyRef("") -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), emptyConfigOrigin, "")),
    ConfigValueFactory.fromIterable(List(1, 2, 3, 4).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.NUMBER)), emptyConfigOrigin, "")))

  checkArbitrary[Float]
  checkArbitrary2[Float, Percentage](_.toFloatFraction)
  checkFailures[Float](
    ConfigValueFactory.fromAnyRef("") -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), emptyConfigOrigin, "")),
    ConfigValueFactory.fromIterable(List(1, 2, 3, 4).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.NUMBER)), emptyConfigOrigin, "")))

  checkArbitrary[Int]

  checkArbitrary[Long]

  checkArbitrary[Short]

  checkArbitrary[Byte]

  checkArbitrary[BigInt]
  checkArbitrary[BigDecimal]
  checkArbitrary[BigInteger]
  checkArbitrary[JavaBigDecimal]

  checkArbitrary[UUID]

  checkArbitrary[Path]

  checkArbitrary[File]

  checkReadWriteString[DayOfWeek]("MONDAY" -> DayOfWeek.MONDAY)
  checkReadWriteString[Month]("JULY" -> Month.JULY)
  checkFailure[DayOfWeek, CannotConvert](
    ConfigValueFactory.fromAnyRef("thursday"), // lowercase string vs upper case enum
    ConfigValueFactory.fromAnyRef("this is not a day")) // no such value

  checkReadWriteString[Pattern]("(a|b)" -> Pattern.compile("(a|b)"))
  checkFailure[Pattern, CannotConvert](ConfigValueFactory.fromAnyRef("(a|b")) // missing closing ')'

  checkReadWriteString[Regex]("(a|b)" -> new Regex("(a|b)"))
  checkFailure[Regex, CannotConvert](ConfigValueFactory.fromAnyRef("(a|b")) // missing closing ')'

  checkReadWriteString[URL](
    "http://host/path?with=query&param" -> new URL("http://host/path?with=query&param"))

  checkReadWriteString[URI](
    "http://host/path?with=query&param" -> new URI("http://host/path?with=query&param"))

  checkReadWrite[ConfigList](
    ConfigValueFactory.fromIterable(List().asJava) -> ConfigValueFactory.fromIterable(List().asJava),
    ConfigValueFactory.fromIterable(List(1, 2, 3).asJava) -> ConfigValueFactory.fromIterable(List(1, 2, 3).asJava))

  checkReadWrite[ConfigValue](
    ConfigValueFactory.fromAnyRef(4) -> ConfigValueFactory.fromAnyRef(4),
    ConfigValueFactory.fromAnyRef("str") -> ConfigValueFactory.fromAnyRef("str"),
    ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava) -> ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))

  checkReadWrite[ConfigMemorySize](
    ConfigValueFactory.fromAnyRef(400L) -> ConfigMemorySize.ofBytes(400L))
  checkRead[ConfigMemorySize](
    ConfigValueFactory.fromAnyRef("400b") -> ConfigMemorySize.ofBytes(400),
    ConfigValueFactory.fromAnyRef("400k") -> ConfigMemorySize.ofBytes(400 * 1024),
    ConfigValueFactory.fromAnyRef("400m") -> ConfigMemorySize.ofBytes(400 * 1024 * 1024),
    ConfigValueFactory.fromAnyRef("400MB") -> ConfigMemorySize.ofBytes(400 * 1000 * 1000))
  checkWrite[ConfigMemorySize](
    ConfigMemorySize.ofBytes(400 * 1024 * 1024) -> ConfigValueFactory.fromAnyRef(400 * 1024 * 1024),
    ConfigMemorySize.ofBytes(400 * 1000 * 1000) -> ConfigValueFactory.fromAnyRef(400 * 1000 * 1000))
  checkFailure[ConfigMemorySize, ExceptionThrown](ConfigValueFactory.fromAnyRef("100noSuchUnit"))

  {
    val conf = ConfigFactory.parseString("""{ v1 = 3, v2 = 4 }""".stripMargin)

    checkReadWrite[ConfigObject](
      conf.root() -> ConfigValueFactory.fromMap(Map("v1" -> 3, "v2" -> 4).asJava))

    checkReadWrite[Config](
      conf.root() -> conf)
  }
}
