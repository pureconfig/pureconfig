package pureconfig.module.joda.configurable

import com.typesafe.config.ConfigFactory
import org.joda.time._
import org.joda.time.format.{ DateTimeFormat, ISODateTimeFormat }

import org.scalatest._
import org.scalacheck.{ Arbitrary, Gen }
import prop.PropertyChecks
import pureconfig.syntax._
import ConfigurableSuite._

import scala.collection.JavaConverters._

class ConfigurableSuite extends FlatSpec with Matchers with TryValues with PropertyChecks {

  val isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ")
  implicit val dateTimeInstance = dateTimeConfigConvert(isoFormatter)

  it should "parse DateTime" in forAll {
    (dateTime: DateTime) =>
      val conf = ConfigFactory.parseString(s"""{date-time:"${isoFormatter.print(dateTime)}"}""")
      case class Conf(dateTime: DateTime)
      conf.to[Conf].success.value shouldEqual Conf(dateTime)
  }

  val timeFormatter = DateTimeFormat.forPattern("HH:mm:ss.SSS")
  implicit val localTimeInstance = localTimeConfigConvert(timeFormatter)

  "pureconfig" should "parse LocalTime" in forAll {
    (localTime: LocalTime) =>
      val conf = ConfigFactory.parseString(s"""{time:"${timeFormatter.print(localTime)}"}""")
      case class Conf(time: LocalTime)
      conf.to[Conf].success.value shouldEqual Conf(localTime)
  }

  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  implicit val localDateInstance = localDateConfigConvert(dateFormatter)

  it should "parse LocalDate" in forAll {
    (localDate: LocalDate) =>
      val conf = ConfigFactory.parseString(s"""{date:"${dateFormatter.print(localDate)}"}""")
      case class Conf(date: LocalDate)
      conf.to[Conf].success.value shouldEqual Conf(localDate)
  }

  val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  implicit val localDateTimeInstance = localDateTimeConfigConvert(dateTimeFormatter)

  it should "parse LocalDateTime" in forAll {
    (localDateTime: LocalDateTime) =>
      val conf = ConfigFactory.parseString(s"""{date-time:"${dateTimeFormatter.print(localDateTime)}"}""")
      case class Conf(dateTime: LocalDateTime)
      //conf.to[Conf].success.value shouldEqual Conf(localDateTime)
      conf.to[Conf].get shouldEqual Conf(localDateTime)
  }

  val monthDayFormat = DateTimeFormat.forPattern("MM-dd")
  implicit val monthDayInstance = monthDayConfigConvert(monthDayFormat)

  it should "parse MonthDay" in forAll {
    (monthDay: MonthDay) =>
      val conf = ConfigFactory.parseString(s"""{month-day:"${monthDayFormat.print(monthDay)}"}""")
      case class Conf(monthDay: MonthDay)
      conf.to[Conf].success.value shouldEqual Conf(monthDay)
  }

  val yearMonthFormat = DateTimeFormat.forPattern("yyyy-MM")
  implicit val yearMonthInstance = yearMonthConfigConvert(yearMonthFormat)

  it should "parse YearMonth" in forAll {
    (yearMonth: YearMonth) =>
      val conf = ConfigFactory.parseString(s"""{year-month:"${yearMonthFormat.print(yearMonth)}"}""")
      case class Conf(yearMonth: YearMonth)
      conf.to[Conf].success.value shouldEqual Conf(yearMonth)
  }
}

object ConfigurableSuite {
  val genHour: Gen[Int] = Gen.chooseNum(0, 23)
  val genMinute: Gen[Int] = Gen.chooseNum(0, 59)
  val genSecond: Gen[Int] = Gen.chooseNum(0, 59)
  val genMilli: Gen[Int] = Gen.chooseNum(0, 999)
  val genYear: Gen[Int] = Gen.chooseNum(1970, 2999)
  val genMonth: Gen[Int] = Gen.chooseNum(1, 12)

  implicit val localTimeArbitrary: Arbitrary[LocalTime] =
    Arbitrary[LocalTime](
      for {
        hour <- genHour
        minute <- genMinute
        second <- genSecond
        milli <- genMilli
      } yield new LocalTime(hour, minute, second, milli))

  implicit val localDateArbitrary: Arbitrary[LocalDate] =
    Arbitrary[LocalDate](
      for {
        year <- genYear
        month <- genMonth
        day <- Gen.chooseNum(1, java.time.YearMonth.of(year, month).lengthOfMonth())
      } yield new LocalDate(year, month, day))

  implicit val localDateTimeArbitrary: Arbitrary[LocalDateTime] =
    Arbitrary[LocalDateTime](
      for {
        date <- localDateArbitrary.arbitrary
        time <- localTimeArbitrary.arbitrary
      } yield date.toLocalDateTime(time))

  implicit val monthDayArbitrary: Arbitrary[MonthDay] =
    Arbitrary[MonthDay](
      localDateArbitrary.arbitrary.map(date => new MonthDay(date.getMonthOfYear, date.getDayOfMonth)))

  implicit val yearMonthArbitrary: Arbitrary[YearMonth] =
    Arbitrary(for {
      year <- genYear
      month <- genMonth
    } yield new YearMonth(year, month))

  implicit val zoneIdArbitrary: Arbitrary[DateTimeZone] =
    Arbitrary(Gen.oneOf(DateTimeZone.getAvailableIDs.asScala.toSeq).map(DateTimeZone.forID))

  implicit val dateTimeArbitrary: Arbitrary[DateTime] =
    Arbitrary(
      for {
        localDateTime <- localDateTimeArbitrary.arbitrary
        zoneId <- zoneIdArbitrary.arbitrary
      } yield localDateTime.toDateTime(zoneId) //new DateTime(localDateTime, zoneId))
    )
}
