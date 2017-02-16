package pureconfig.module.joda.configurable

import com.typesafe.config.ConfigFactory
import org.joda.time._
import org.joda.time.format.DateTimeFormat

import org.scalatest._
import org.scalacheck.{ Arbitrary, Gen }
import prop.PropertyChecks
import pureconfig.syntax._
import ConfigurableSuite._
import pureconfig.configurable.{ ConfigurableSuite => JTime }

import scala.collection.JavaConverters._

class ConfigurableSuite extends FlatSpec with Matchers with EitherValues with PropertyChecks {

  val isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ")
  implicit val dateTimeInstance = dateTimeConfigConvert(isoFormatter)

  it should "parse DateTime" in forAll {
    (dateTime: DateTime) =>
      val conf = ConfigFactory.parseString(s"""{date-time:"${isoFormatter.print(dateTime)}"}""")
      case class Conf(dateTime: DateTime)
      conf.to[Conf].right.value shouldEqual Conf(dateTime)
  }

  val timeFormatter = DateTimeFormat.forPattern("HH:mm:ss.SSS")
  implicit val localTimeInstance = localTimeConfigConvert(timeFormatter)

  "pureconfig" should "parse LocalTime" in forAll {
    (localTime: LocalTime) =>
      val conf = ConfigFactory.parseString(s"""{time:"${timeFormatter.print(localTime)}"}""")
      case class Conf(time: LocalTime)
      conf.to[Conf].right.value shouldEqual Conf(localTime)
  }

  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  implicit val localDateInstance = localDateConfigConvert(dateFormatter)

  it should "parse LocalDate" in forAll {
    (localDate: LocalDate) =>
      val conf = ConfigFactory.parseString(s"""{date:"${dateFormatter.print(localDate)}"}""")
      case class Conf(date: LocalDate)
      conf.to[Conf].right.value shouldEqual Conf(localDate)
  }

  val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  implicit val localDateTimeInstance = localDateTimeConfigConvert(dateTimeFormatter)

  it should "parse LocalDateTime" in forAll {
    (localDateTime: LocalDateTime) =>
      val conf = ConfigFactory.parseString(s"""{date-time:"${dateTimeFormatter.print(localDateTime)}"}""")
      case class Conf(dateTime: LocalDateTime)
      conf.to[Conf].right.value shouldEqual Conf(localDateTime)
  }

  val monthDayFormat = DateTimeFormat.forPattern("MM-dd")
  implicit val monthDayInstance = monthDayConfigConvert(monthDayFormat)

  it should "parse MonthDay" in forAll {
    (monthDay: MonthDay) =>
      val conf = ConfigFactory.parseString(s"""{month-day:"${monthDayFormat.print(monthDay)}"}""")
      case class Conf(monthDay: MonthDay)
      conf.to[Conf].right.value shouldEqual Conf(monthDay)
  }

  val yearMonthFormat = DateTimeFormat.forPattern("yyyy-MM")
  implicit val yearMonthInstance = yearMonthConfigConvert(yearMonthFormat)

  it should "parse YearMonth" in forAll {
    (yearMonth: YearMonth) =>
      val conf = ConfigFactory.parseString(s"""{year-month:"${yearMonthFormat.print(yearMonth)}"}""")
      case class Conf(yearMonth: YearMonth)
      conf.to[Conf].right.value shouldEqual Conf(yearMonth)
  }
}

object ConfigurableSuite {
  private def nanoToMilli(n: Int): Int = n / 1000000

  implicit val localTimeArbitrary: Arbitrary[LocalTime] = Arbitrary(
    JTime.localTimeArbitrary.arbitrary.map { t =>
      new LocalTime(t.getHour, t.getMinute, t.getSecond, nanoToMilli(t.getNano))
    })

  implicit val localDateArbitrary: Arbitrary[LocalDate] = Arbitrary(
    JTime.localDateArbitrary.arbitrary.map { d =>
      new LocalDate(d.getYear, d.getMonthValue, d.getDayOfMonth)
    })

  implicit val localDateTimeArbitrary: Arbitrary[LocalDateTime] = Arbitrary(
    JTime.localDateTimeArbitrary.arbitrary.map { d =>
      new LocalDateTime(
        d.getYear, d.getMonthValue, d.getDayOfMonth,
        d.getHour, d.getMinute, d.getSecond, nanoToMilli(d.getNano))
    })

  implicit val monthDayArbitrary: Arbitrary[MonthDay] = Arbitrary(
    JTime.monthDayArbitrary.arbitrary.map { m =>
      new MonthDay(m.getMonthValue, m.getDayOfMonth)
    })

  implicit val yearMonthArbitrary: Arbitrary[YearMonth] = Arbitrary(
    JTime.yearMonthArbitrary.arbitrary.map { y =>
      new YearMonth(y.getYear, y.getMonthValue)
    })

  implicit val zoneIdArbitrary: Arbitrary[DateTimeZone] =
    Arbitrary(Gen.oneOf(DateTimeZone.getAvailableIDs.asScala.toSeq).map(DateTimeZone.forID))

  implicit val dateTimeArbitrary: Arbitrary[DateTime] =
    Arbitrary(
      for {
        localDateTime <- localDateTimeArbitrary.arbitrary
        zoneId <- zoneIdArbitrary.arbitrary
      } yield localDateTime.toDateTime(zoneId))
}
