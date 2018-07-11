package pureconfig.module.joda

import org.joda.time._
import org.scalacheck.Arbitrary
import JodaSuite._
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }
import pureconfig.module.joda.configurable.ConfigurableSuite.{ dateTimeArbitrary, zoneIdArbitrary }
import pureconfig.BaseSuite

class JodaSuite extends BaseSuite {

  checkArbitrary[Instant]

  checkArbitrary[Interval]

  checkArbitrary[Duration]

  checkArbitrary[DateTimeZone]

  checkReadString[DateTimeFormatter](
    "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ" -> DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"))
}

object JodaSuite {
  implicit val instantArbitrary: Arbitrary[Instant] =
    Arbitrary(Arbitrary.arbitrary[Long].map(new Instant(_)))

  implicit val intervalArbitrary: Arbitrary[Interval] =
    Arbitrary(Arbitrary.arbitrary[(DateTime, DateTime)]
      .map { i =>
        val (from, to) = if (i._1.isBefore(i._2)) i else i.swap
        // Comparing new Interval(foo) with Interval.parseWithOffset(foo) will return false
        // See http://www.joda.org/joda-time/apidocs/org/joda/time/Interval.html#parseWithOffset-java.lang.String-
        Interval.parseWithOffset(new Interval(from, to).toString)
      })

  implicit val durationArbitrary: Arbitrary[Duration] =
    Arbitrary(Arbitrary.arbitrary[Long].map(new Duration(_)))
}
