package pureconfig.module.joda.configurable

import org.joda.time._
import org.joda.time.format.{ DateTimeFormat, ISOPeriodFormat, PeriodFormatter }
import org.scalacheck.{ Arbitrary, Gen }
import ConfigurableSuite._
import pureconfig.configurable.{ ConfigurableSuite => JTime }
import scala.collection.JavaConverters._

import pureconfig.BaseSuite

class ConfigurableSuite extends BaseSuite {

  val isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ")
  implicit val dateTimeInstance = dateTimeConfigConvert(isoFormatter)
  checkArbitrary[DateTime]

  val timeFormatter = DateTimeFormat.forPattern("HH:mm:ss.SSS")
  implicit val localTimeInstance = localTimeConfigConvert(timeFormatter)
  checkArbitrary[LocalTime]

  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  implicit val localDateInstance = localDateConfigConvert(dateFormatter)
  checkArbitrary[LocalDate]

  val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  implicit val localDateTimeInstance = localDateTimeConfigConvert(dateTimeFormatter)
  checkArbitrary[LocalDateTime]

  val monthDayFormat = DateTimeFormat.forPattern("MM-dd")
  implicit val monthDayInstance = monthDayConfigConvert(monthDayFormat)
  checkArbitrary[MonthDay]

  val yearMonthFormat = DateTimeFormat.forPattern("yyyy-MM")
  implicit val yearMonthInstance = yearMonthConfigConvert(yearMonthFormat)
  checkArbitrary[YearMonth]

  val periodFormatter: PeriodFormatter = ISOPeriodFormat.standard()
  implicit val periodInstance = periodConfigConvert(periodFormatter)
  checkArbitrary[Period]
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
        zoneId <- zoneIdArbitrary.arbitrary
        localDateTime <- localDateTimeArbitrary.arbitrary if !zoneId.isLocalDateTimeGap(localDateTime)
      } yield localDateTime.toDateTime(zoneId))

  implicit val periodArbitrary: Arbitrary[Period] =
    Arbitrary(Arbitrary.arbitrary[Int].map { i =>
      // Generated large longs crash Period constructor internally
      new Period(i.toLong)
    })
}
