package pureconfig

import java.nio.file.{ Path, Paths }
import java.time._
import java.util.UUID

import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.configurable.ConfigurableSuite
import pureconfig.data._

import scala.collection.JavaConverters._
import scala.concurrent.duration.{ Duration, FiniteDuration }

package object gen {

  val genFiniteDuration: Gen[FiniteDuration] =
    Gen.choose(Long.MinValue + 1, Long.MaxValue) map Duration.fromNanos

  val genDuration: Gen[Duration] =
    Gen.frequency(
      1 -> Gen.oneOf(Duration.Inf, Duration.MinusInf, Duration.Undefined),
      99 -> genFiniteDuration)

  val genInstant: Gen[Instant] =
    Gen.choose(Instant.MIN.getEpochSecond, Instant.MAX.getEpochSecond).map(Instant.ofEpochSecond)

  val genZoneId: Gen[ZoneId] =
    Gen.oneOf(ZoneId.SHORT_IDS.values().asScala.toSeq.distinct).map(ZoneId.of)

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

  val genHost: Gen[String] =
    Gen.alphaNumStr

  val genPort: Gen[Int] =
    Gen.chooseNum(0, 65535)

  val genUUID: Gen[UUID] =
    for {
      mostSigBits <- Arbitrary.arbLong.arbitrary
      leastSigBits <- Arbitrary.arbLong.arbitrary
    } yield new UUID(mostSigBits, leastSigBits)

  val genPath: Gen[Path] =
    Gen.nonEmptyListOf(Gen.alphaNumStr).map(parts => parts.map(str => Paths.get(str)).reduce(_ resolve _))

  val genPercentage: Gen[Percentage] =
    Gen.choose[Int](0, 100).map(Percentage.apply)

  val genJodaDateTime: Gen[org.joda.time.DateTime] =
    for {
      dateTime <- ConfigurableSuite.localDateTimeArbitrary.arbitrary
    } yield new org.joda.time.DateTime(dateTime.getYear, dateTime.getMonthValue, dateTime.getDayOfMonth,
      dateTime.getHour, dateTime.getMinute, dateTime.getSecond, org.joda.time.DateTimeZone.UTC)
}
