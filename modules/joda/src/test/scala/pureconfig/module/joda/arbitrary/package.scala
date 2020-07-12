package pureconfig.module.joda

import scala.collection.JavaConverters._

import org.joda.time._
import org.scalacheck.{Arbitrary, Gen}
import pureconfig.gen._

package object arbitrary {
  private def nanoToMilli(n: Int): Int = n / 1000000

  implicit val instantArbitrary: Arbitrary[Instant] =
    Arbitrary(Arbitrary.arbitrary[Long].map(new Instant(_)))

  implicit val intervalArbitrary: Arbitrary[Interval] =
    Arbitrary(
      Arbitrary
        .arbitrary[(DateTime, DateTime)]
        .map { i =>
          val (from, to) = if (i._1.isBefore(i._2)) i else i.swap
          // Comparing new Interval(foo) with Interval.parseWithOffset(foo) will return false
          // See http://www.joda.org/joda-time/apidocs/org/joda/time/Interval.html#parseWithOffset-java.lang.String-
          Interval.parseWithOffset(new Interval(from, to).toString)
        }
    )

  implicit val durationArbitrary: Arbitrary[Duration] =
    Arbitrary(Arbitrary.arbitrary[Long].map(new Duration(_)))

  implicit val localTimeArbitrary: Arbitrary[LocalTime] = Arbitrary(genLocalTime.map { t =>
    new LocalTime(t.getHour, t.getMinute, t.getSecond, nanoToMilli(t.getNano))
  })

  implicit val localDateArbitrary: Arbitrary[LocalDate] = Arbitrary(genLocalDate.map { d =>
    new LocalDate(d.getYear, d.getMonthValue, d.getDayOfMonth)
  })

  implicit val localDateTimeArbitrary: Arbitrary[LocalDateTime] = Arbitrary(genLocalDateTime.map { d =>
    new LocalDateTime(
      d.getYear,
      d.getMonthValue,
      d.getDayOfMonth,
      d.getHour,
      d.getMinute,
      d.getSecond,
      nanoToMilli(d.getNano)
    )
  })

  implicit val monthDayArbitrary: Arbitrary[MonthDay] = Arbitrary(genMonthDay.map { m =>
    new MonthDay(m.getMonthValue, m.getDayOfMonth)
  })

  implicit val yearMonthArbitrary: Arbitrary[YearMonth] = Arbitrary(genYearMonth.map { y =>
    new YearMonth(y.getYear, y.getMonthValue)
  })

  implicit val zoneIdArbitrary: Arbitrary[DateTimeZone] =
    Arbitrary(Gen.oneOf(DateTimeZone.getAvailableIDs.asScala.toSeq).map(DateTimeZone.forID))

  implicit val dateTimeArbitrary: Arbitrary[DateTime] =
    Arbitrary(for {
      zoneId <- zoneIdArbitrary.arbitrary
      localDateTime <- localDateTimeArbitrary.arbitrary if !zoneId.isLocalDateTimeGap(localDateTime)
    } yield localDateTime.toDateTime(zoneId))

  implicit val periodArbitrary: Arbitrary[Period] =
    Arbitrary(Arbitrary.arbitrary[Int].map { i =>
      // Generated large longs crash Period constructor internally
      new Period(i.toLong)
    })
}
