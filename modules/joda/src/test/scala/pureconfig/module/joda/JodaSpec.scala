package pureconfig.module.joda

import com.typesafe.config.{ ConfigFactory, ConfigValueFactory }
import org.joda.time._
import org.scalacheck.Arbitrary
import JodaSpec._
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }
import pureconfig.module.joda.configurable.ConfigurableSuite.{ dateTimeArbitrary, zoneIdArbitrary }
import pureconfig.BaseSuite
import pureconfig.syntax._

class JodaSpec extends BaseSuite {

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

  checkRead[DateTimeFormatter](
    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ") ->
      ConfigValueFactory.fromAnyRef("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"))
}

object JodaSpec {
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
