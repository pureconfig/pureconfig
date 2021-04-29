package pureconfig

import java.io.File
import java.math.{BigDecimal => JavaBigDecimal, BigInteger}
import java.nio.file.{Path, Paths}
import java.time._
import java.time.{Duration => JavaDuration}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

import org.scalacheck.{Arbitrary, Gen}

import pureconfig.data._

package object gen {
  val genFiniteDuration: Gen[FiniteDuration] =
    Gen
      .choose(Long.MinValue + 1, Long.MaxValue)
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
    Gen.frequency(1 -> Gen.oneOf(Duration.Inf, Duration.MinusInf, Duration.Undefined), 99 -> genFiniteDuration)

  val genInstant: Gen[Instant] =
    Gen.choose(Instant.MIN.getEpochSecond, Instant.MAX.getEpochSecond).map(Instant.ofEpochSecond)

  val genPeriod: Gen[Period] =
    for {
      years <- Arbitrary.arbInt.arbitrary
      months <- Arbitrary.arbInt.arbitrary
      days <- Arbitrary.arbInt.arbitrary
    } yield Period.of(years, months, days)

  val genPath: Gen[Path] =
    Gen.nonEmptyListOf(Gen.alphaNumStr).map(parts => parts.map(str => Paths.get(str)).reduce(_ resolve _))

  val genFile: Gen[File] =
    genPath.map(_.toFile)

  val genPercentage: Gen[Percentage] =
    Gen.choose[Int](0, 100).map(Percentage.apply)

  val genJavaBigDecimal: Gen[JavaBigDecimal] = Arbitrary.arbitrary[BigDecimal].map(_.bigDecimal)

  val genBigInt: Gen[BigInteger] = Arbitrary.arbitrary[BigInt].map(_.bigInteger)

  val genHour: Gen[Int] = Gen.chooseNum(0, 23)
  val genMinute: Gen[Int] = Gen.chooseNum(0, 59)
  val genSecond: Gen[Int] = Gen.chooseNum(0, 59)
  val genNano: Gen[Int] = Gen.chooseNum(0, 999999999)
  val genYearInt: Gen[Int] = Gen.chooseNum(1970, 2999)
  val genMonth: Gen[Int] = Gen.chooseNum(1, 12)

  val genLocalTime: Gen[LocalTime] = for {
    hour <- genHour
    minute <- genMinute
    second <- genSecond
    nano <- genNano
  } yield LocalTime.of(hour, minute, second, nano)

  val genLocalDate: Gen[LocalDate] = for {
    year <- genYearInt
    month <- genMonth
    day <- Gen.chooseNum(1, java.time.YearMonth.of(year, month).lengthOfMonth())
  } yield LocalDate.of(year, month, day)

  val genLocalDateTime: Gen[LocalDateTime] = for {
    date <- genLocalDate
    time <- genLocalTime
  } yield date.atTime(time)

  val genMonthDay: Gen[MonthDay] =
    genLocalDate.map(date => MonthDay.of(date.getMonthValue, date.getDayOfMonth))

  val genZoneOffset: Gen[ZoneOffset] =
    Gen.choose(ZoneOffset.MIN.getTotalSeconds, ZoneOffset.MAX.getTotalSeconds).map(ZoneOffset.ofTotalSeconds)

  val genOffsetDateTime: Gen[OffsetDateTime] = for {
    localDateTime <- genLocalDateTime
    offset <- genZoneOffset
  } yield OffsetDateTime.of(localDateTime, offset)

  val genOffsetTime: Gen[OffsetTime] = for {
    localTime <- genLocalTime
    offset <- genZoneOffset
  } yield OffsetTime.of(localTime, offset)

  val genYear: Gen[Year] =
    Gen.choose(Year.MIN_VALUE, Year.MAX_VALUE).map(Year.of)

  val genYearMonth: Gen[YearMonth] = for {
    year <- genYearInt
    month <- genMonth
  } yield YearMonth.of(year, month)

  val genZoneId: Gen[ZoneId] =
    Gen.oneOf(ZoneId.getAvailableZoneIds.asScala.toSeq).map(ZoneId.of)

  val genZonedDateTime: Gen[ZonedDateTime] = for {
    localDateTime <- genLocalDateTime
    zoneId <- genZoneId.suchThat(_ != ZoneId.of("GMT0")) // Avoid JDK-8138664
  } yield ZonedDateTime.of(localDateTime, zoneId)
}
