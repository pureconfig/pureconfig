package pureconfig.module.joda.configurable

import com.typesafe.config.{ ConfigFactory, ConfigValueFactory }
import org.joda.time._
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter, ISOPeriodFormat, PeriodFormatter }
import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.syntax._
import ConfigurableSuite._
import pureconfig.module.joda._
import pureconfig.configurable.{ ConfigurableSuite => JTime }
import scala.collection.JavaConverters._

import pureconfig.BaseSuite

class ConfigurableSuite extends BaseSuite {

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

  it should "parse LocalTime" in forAll {
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

  it should "parse Instant" in forAll {
    (instant: Instant) =>
      val conf = ConfigFactory.parseString(s"""{instant:${instant.getMillis}}""")
      case class Conf(instant: Instant)
      conf.to[Conf].right.value shouldEqual Conf(instant)
  }

  it should "parse Interval" in forAll {
    (interval: Interval) =>
      val conf = ConfigFactory.parseString(s"""{interval:"${interval.toString}"}""")
      case class Conf(interval: Interval)
      conf.to[Conf].right.value shouldEqual Conf(interval)
  }

  it should "parse Duration" in forAll {
    (duration: Duration) =>
      val conf = ConfigFactory.parseString(s"""{duration:"${duration.toString}"}""")
      case class Conf(duration: Duration)
      conf.to[Conf].right.value shouldEqual Conf(duration)
  }

  it should "parse DateTimeZone" in forAll {
    (dateTimeZone: DateTimeZone) =>
      val conf = ConfigFactory.parseString(s"""{date-time-zone:"${dateTimeZone.getID}"}""")
      case class Conf(dateTimeZone: DateTimeZone)
      conf.to[Conf].right.value shouldEqual Conf(dateTimeZone)
  }

  val periodFormatter: PeriodFormatter = ISOPeriodFormat.standard()
  implicit val periodInstance = periodConfigConvert(periodFormatter)

  it should "parse Period" in forAll {
    (period: Period) =>
      val conf = ConfigFactory.parseString(s"""{period:"${periodFormatter.print(period)}"}""")
      case class Conf(period: Period)
      conf.to[Conf].right.value.period.toStandardDuration shouldEqual Conf(period).period.toStandardDuration
  }

  checkRead[DateTimeFormatter](
    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ") ->
      ConfigValueFactory.fromAnyRef("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"))
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

  implicit val instantArbitrary: Arbitrary[Instant] =
    Arbitrary(Arbitrary.arbitrary[Long].map(new Instant(_)))

  implicit val intervalArbitrary: Arbitrary[Interval] =
    Arbitrary(Arbitrary.arbitrary[(DateTime, DateTime)]
      .filter(i => i._1.isBefore(i._2))
      .map { i =>
        // Comparing new Interval(foo) with Interval.parseWithOffset(foo) will return false
        // See http://www.joda.org/joda-time/apidocs/org/joda/time/Interval.html#parseWithOffset-java.lang.String-
        Interval.parseWithOffset(new Interval(i._1, i._2).toString)
      })

  implicit val durationArbitrary: Arbitrary[Duration] =
    Arbitrary(Arbitrary.arbitrary[Long].map(new Duration(_)))

  implicit val periodArbitrary: Arbitrary[Period] = {
    Arbitrary(
      for {
        // Marshelling and unmarshalling fails with big values
        i1 <- Gen.choose(-50, 50)
        i2 <- Gen.choose(-50, 50)
        i3 <- Gen.choose(-50, 50)
        i4 <- Gen.choose(-50, 50)
      } yield new Period(i1, i2, i3, i4))
  }
}
