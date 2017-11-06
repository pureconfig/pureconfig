package pureconfig

import java.io.File
import java.math.{ BigInteger, BigDecimal => JavaBigDecimal }
import java.net.InetAddress
import java.nio.file.{ Path, Paths }
import java.time._
import java.time.{ Duration => JavaDuration }

import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.configurable.ConfigurableSuite
import pureconfig.data._
import scala.collection.JavaConverters._
import scala.concurrent.duration.{ Duration, FiniteDuration }

package object gen {
  val genFiniteDuration: Gen[FiniteDuration] =
    Gen.choose(Long.MinValue + 1, Long.MaxValue)
      .suchThat(_ != 8092048641075763L) // doesn't work, see #182
      .map(Duration.fromNanos)

  val MaximumNanoseconds = 999999999L
  val genJavaDuration: Gen[JavaDuration] = for {
    seconds <- Gen.choose(Long.MinValue + 1, Long.MaxValue)
    nanoseconds <- Gen.choose(0L, MaximumNanoseconds)
    // JDK Bug: when seconds % 60 == -1 and nanoseconds > 0, Duration.toString produces
    // a strange value for the seconds with is -0. followed by 1000_000_000 - nanoseconds
    // e.g. Duration.ofSeconds(-1, 1).toString returns PT-0.999999999S
    // Duration.parse loads this value as PT0.999999999S instead of the original value
    if nanoseconds == 0 || seconds % 60 != -1
  } yield JavaDuration.ofSeconds(seconds, nanoseconds)

  val genDuration: Gen[Duration] =
    Gen.frequency(
      1 -> Gen.oneOf(Duration.Inf, Duration.MinusInf, Duration.Undefined),
      99 -> genFiniteDuration)

  val genInstant: Gen[Instant] =
    Gen.choose(Instant.MIN.getEpochSecond, Instant.MAX.getEpochSecond).map(Instant.ofEpochSecond)

  val genZoneId: Gen[ZoneId] =
    Gen.oneOf(ZoneId.getAvailableZoneIds.asScala.toSeq).map(ZoneId.of)

  val genZoneOffset: Gen[ZoneOffset] =
    Gen.choose(ZoneOffset.MIN.getTotalSeconds, ZoneOffset.MAX.getTotalSeconds).map(ZoneOffset.ofTotalSeconds)

  val genPeriod: Gen[Period] =
    for {
      years <- Arbitrary.arbInt.arbitrary
      months <- Arbitrary.arbInt.arbitrary
      days <- Arbitrary.arbInt.arbitrary
    } yield Period.of(years, months, days)

  val genYear: Gen[Year] =
    Gen.choose(Year.MIN_VALUE, Year.MAX_VALUE).map(Year.of)

  val genPath: Gen[Path] =
    Gen.nonEmptyListOf(Gen.alphaNumStr).map(parts => parts.map(str => Paths.get(str)).reduce(_ resolve _))

  val genFile: Gen[File] =
    genPath.map(_.toFile)

  val genPercentage: Gen[Percentage] =
    Gen.choose[Int](0, 100).map(Percentage.apply)

  val genJodaDateTime: Gen[org.joda.time.DateTime] =
    for {
      dateTime <- ConfigurableSuite.localDateTimeArbitrary.arbitrary
    } yield new org.joda.time.DateTime(dateTime.getYear, dateTime.getMonthValue, dateTime.getDayOfMonth,
      dateTime.getHour, dateTime.getMinute, dateTime.getSecond, org.joda.time.DateTimeZone.UTC)

  val genJavaBigDecimal: Gen[JavaBigDecimal] = Arbitrary.arbitrary[BigDecimal].map(_.bigDecimal)

  val genBigInt: Gen[BigInteger] = Arbitrary.arbitrary[BigInt].map(_.bigInteger)

  val genIPv4Address: Gen[InetAddress] = for {
    bytes <- Gen.listOfN(4, Arbitrary.arbitrary[Byte])
  } yield InetAddress.getByAddress(bytes.toArray)

  val genIPv6Address: Gen[InetAddress] = for {
    bytes <- Gen.listOfN(16, Arbitrary.arbitrary[Byte])
  } yield InetAddress.getByAddress(bytes.toArray)

  val genInetAddress: Gen[InetAddress] = Gen.oneOf(genIPv4Address, genIPv6Address)
}
