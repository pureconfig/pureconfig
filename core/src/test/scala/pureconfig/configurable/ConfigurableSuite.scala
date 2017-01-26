package pureconfig.configurable

import com.typesafe.config.ConfigFactory
import java.time._
import java.time.format.DateTimeFormatter

import org.scalatest._
import org.scalacheck.{ Arbitrary, Gen }
import prop.PropertyChecks
import pureconfig.syntax._
import ConfigurableSuite._

import scala.collection.JavaConverters._

class ConfigurableSuite extends FlatSpec with Matchers with TryValues with PropertyChecks {

  implicit val localTimeInstance = localTimeConfigConvert(DateTimeFormatter.ISO_TIME)

  "pureconfig" should "parse LocalTime" in forAll {
    (localTime: LocalTime) =>
      val conf = ConfigFactory.parseString(s"""{time:"${localTime.format(DateTimeFormatter.ISO_TIME)}"}""")
      case class Conf(time: LocalTime)
      conf.to[Conf].success.value shouldEqual Conf(localTime)
  }

  implicit val localDateInstance = localDateConfigConvert(DateTimeFormatter.ISO_DATE)

  it should "parse LocalDate" in forAll {
    (localDate: LocalDate) =>
      val conf = ConfigFactory.parseString(s"""{date:"${localDate.format(DateTimeFormatter.ISO_DATE)}"}""")
      case class Conf(date: LocalDate)
      conf.to[Conf].success.value shouldEqual Conf(localDate)
  }

  implicit val localDateTimeInstance = localDateTimeConfigConvert(DateTimeFormatter.ISO_DATE_TIME)

  it should "parse LocalDateTime" in forAll {
    (localDateTime: LocalDateTime) =>
      val conf = ConfigFactory.parseString(s"""{dateTime:"${localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)}"}""")
      case class Conf(dateTime: LocalDateTime)
      conf.to[Conf].success.value shouldEqual Conf(localDateTime)
  }

  val monthDayFormat = DateTimeFormatter.ofPattern("MM-dd")
  implicit val monthDayInstance = monthDayConfigConvert(monthDayFormat)

  it should "parse MonthDay" in forAll {
    (monthDay: MonthDay) =>
      val conf = ConfigFactory.parseString(s"""{monthDay:"${monthDay.format(monthDayFormat)}"}""")
      case class Conf(monthDay: MonthDay)
      conf.to[Conf].success.value shouldEqual Conf(monthDay)
  }

  implicit val offsetDateTimeInstance = offsetDateTimeConfigConvert(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

  it should "parse OffsetDateTime" in forAll {
    (offsetDateTime: OffsetDateTime) =>
      val conf = ConfigFactory.parseString(s"""{offsetDateTime:"${offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"}""")
      case class Conf(offsetDateTime: OffsetDateTime)
      conf.to[Conf].success.value shouldEqual Conf(offsetDateTime)
  }

  implicit val offsetTimeInstance = offsetTimeConfigConvert(DateTimeFormatter.ISO_OFFSET_TIME)

  it should "parse OffsetTime" in forAll {
    (offsetTime: OffsetTime) =>
      val conf = ConfigFactory.parseString(s"""{offsetTime:"${offsetTime.format(DateTimeFormatter.ISO_OFFSET_TIME)}"}""")
      case class Conf(offsetTime: OffsetTime)
      conf.to[Conf].success.value shouldEqual Conf(offsetTime)
  }

  val yearMonthFormat = DateTimeFormatter.ofPattern("yyyy-MM")
  implicit val yearMonthInstance = yearMonthConfigConvert(yearMonthFormat)

  it should "parse YearMonth" in forAll {
    (yearMonth: YearMonth) =>
      val conf = ConfigFactory.parseString(s"""{yearMonth:"${yearMonth.format(yearMonthFormat)}"}""")
      case class Conf(yearMonth: YearMonth)
      conf.to[Conf].success.value shouldEqual Conf(yearMonth)
  }

  implicit val zonedDateTimeInstance = zonedDateTimeConfigConvert(DateTimeFormatter.ISO_ZONED_DATE_TIME)

  it should "parse ZonedDateTime" in forAll {
    (zonedDateTime: ZonedDateTime) =>
      val conf = ConfigFactory.parseString(s"""{zonedDateTime:"${zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)}"}""")
      case class Conf(zonedDateTime: ZonedDateTime)
      conf.to[Conf].success.value shouldEqual Conf(zonedDateTime)
  }
}

object ConfigurableSuite {

  val genHour: Gen[Int] = Gen.chooseNum(0, 23)
  val genMinute: Gen[Int] = Gen.chooseNum(0, 59)
  val genSecond: Gen[Int] = Gen.chooseNum(0, 59)
  val genNano: Gen[Int] = Gen.chooseNum(0, 999999999)
  val genYear: Gen[Int] = Gen.chooseNum(1970, 2999)
  val genMonth: Gen[Int] = Gen.chooseNum(1, 12)

  implicit val localTimeArbitrary: Arbitrary[LocalTime] =
    Arbitrary[LocalTime](
      for {
        hour <- genHour
        minute <- genMinute
        second <- genSecond
        nano <- genNano
      } yield LocalTime.of(hour, minute, second, nano))

  implicit val localDateArbitrary: Arbitrary[LocalDate] =
    Arbitrary[LocalDate](
      for {
        year <- genYear
        month <- genMonth
        day <- Gen.chooseNum(1, java.time.YearMonth.of(year, month).lengthOfMonth())
      } yield LocalDate.of(year, month, day))

  implicit val localDateTimeArbitrary: Arbitrary[LocalDateTime] =
    Arbitrary[LocalDateTime](
      for {
        date <- localDateArbitrary.arbitrary
        time <- localTimeArbitrary.arbitrary
      } yield date.atTime(time))

  implicit val monthDayArbitrary: Arbitrary[MonthDay] =
    Arbitrary[MonthDay](
      localDateArbitrary.arbitrary.map(date => MonthDay.of(date.getMonthValue, date.getDayOfMonth)))

  implicit val zoneOffsetArbitrary: Arbitrary[ZoneOffset] =
    Arbitrary(
      Gen.choose(ZoneOffset.MIN.getTotalSeconds, ZoneOffset.MAX.getTotalSeconds).map(ZoneOffset.ofTotalSeconds))

  implicit val offsetDateTimeArbitrary: Arbitrary[OffsetDateTime] =
    Arbitrary[OffsetDateTime](
      for {
        localDateTime <- localDateTimeArbitrary.arbitrary
        offset <- zoneOffsetArbitrary.arbitrary
      } yield OffsetDateTime.of(localDateTime, offset))

  implicit val offsetTimeArbitrary: Arbitrary[OffsetTime] =
    Arbitrary[OffsetTime](
      for {
        localTime <- localTimeArbitrary.arbitrary
        offset <- zoneOffsetArbitrary.arbitrary
      } yield OffsetTime.of(localTime, offset))

  implicit val yearMonthArbitrary: Arbitrary[YearMonth] =
    Arbitrary(for {
      year <- genYear
      month <- genMonth
    } yield YearMonth.of(year, month))

  implicit val zoneIdArbitrary: Arbitrary[ZoneId] =
    Arbitrary(Gen.oneOf(ZoneId.getAvailableZoneIds.asScala.toSeq).map(ZoneId.of))

  implicit val zonedDateTimeArbitrary: Arbitrary[ZonedDateTime] =
    Arbitrary(
      for {
        localDateTime <- localDateTimeArbitrary.arbitrary
        zoneId <- zoneIdArbitrary.arbitrary
      } yield ZonedDateTime.of(localDateTime, zoneId))
}
