package pureconfig.configurable

import com.typesafe.config.ConfigFactory
import java.time.{ LocalDate, LocalDateTime, LocalTime }
import java.time.format.DateTimeFormatter
import org.scalatest._
import org.scalacheck.{ Arbitrary, Gen }
import prop.PropertyChecks
import pureconfig.syntax._

import ConfigurableSuite._

class ConfigurableSuite extends FlatSpec with Matchers with TryValues with PropertyChecks {

  implicit val localTimeInstance = makeLocalTimeConfigConvert(DateTimeFormatter.ISO_TIME)

  "pureconfig" should "parse LocalTime" in forAll {
    (localTime: LocalTime) =>
      val conf = ConfigFactory.parseString(s"""{time:"${localTime.format(DateTimeFormatter.ISO_TIME)}"}""")
      case class Conf(time: LocalTime)
      conf.to[Conf].success.value shouldEqual Conf(localTime)
  }

  implicit val localDateInstance = makeLocalDateConfigConvert(DateTimeFormatter.ISO_DATE)

  it should "parse LocalDate" in forAll {
    (localDate: LocalDate) =>
      val conf = ConfigFactory.parseString(s"""{date:"${localDate.format(DateTimeFormatter.ISO_DATE)}"}""")
      case class Conf(date: LocalDate)
      conf.to[Conf].success.value shouldEqual Conf(localDate)
  }

  implicit val localDateTimeInstance = makeLocalDateTimeConfigConvert(DateTimeFormatter.ISO_DATE_TIME)

  it should "parse LocalDateTime" in forAll {
    (localDateTime: LocalDateTime) =>
      val conf = ConfigFactory.parseString(s"""{dateTime:"${localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)}"}""")
      case class Conf(dateTime: LocalDateTime)
      conf.to[Conf].success.value shouldEqual Conf(localDateTime)
  }
}

object ConfigurableSuite {

  val genHour: Gen[Int] = Gen.chooseNum(0, 23)
  val genMinute: Gen[Int] = Gen.chooseNum(0, 59)
  val genSecond: Gen[Int] = Gen.chooseNum(0, 59)
  val genNano: Gen[Int] = Gen.chooseNum(0, 999999999)

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
        year <- Gen.chooseNum(1970, 2999)
        month <- Gen.chooseNum(1, 12)
        day <- Gen.chooseNum(1, java.time.YearMonth.of(year, month).lengthOfMonth())
      } yield LocalDate.of(year, month, day))

  implicit val localDateTimeArbitrary: Arbitrary[LocalDateTime] =
    Arbitrary[LocalDateTime](
      for {
        date <- localDateArbitrary.arbitrary
        time <- localTimeArbitrary.arbitrary
      } yield date.atTime(time))
}
