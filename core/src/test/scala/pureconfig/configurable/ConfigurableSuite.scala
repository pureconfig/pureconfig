package pureconfig.configurable

import java.time._
import java.time.format.DateTimeFormatter

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions.concise
import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.BaseSuite
import pureconfig.configurable.ConfigurableSuite._
import pureconfig.error.UnknownKey
import pureconfig.syntax._

class ConfigurableSuite extends BaseSuite {
  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  behavior of "configurable converters"

  implicit val localTimeInstance = localTimeConfigConvert(DateTimeFormatter.ISO_TIME)
  implicit val localDateInstance = localDateConfigConvert(DateTimeFormatter.ISO_DATE)
  implicit val localDateTimeInstance = localDateTimeConfigConvert(DateTimeFormatter.ISO_DATE_TIME)
  implicit val monthDayInstance = monthDayConfigConvert(DateTimeFormatter.ofPattern("MM-dd"))
  implicit val offsetDateTimeInstance = offsetDateTimeConfigConvert(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  implicit val offsetTimeInstance = offsetTimeConfigConvert(DateTimeFormatter.ISO_OFFSET_TIME)
  implicit val yearMonthInstance = yearMonthConfigConvert(DateTimeFormatter.ofPattern("yyyy-MM"))
  implicit val zonedDateTimeInstance = zonedDateTimeConfigConvert(DateTimeFormatter.ISO_ZONED_DATE_TIME)

  checkArbitrary[LocalTime]
  checkArbitrary[LocalDate]
  checkArbitrary[LocalDateTime]
  checkArbitrary[MonthDay]
  checkArbitrary[OffsetDateTime]
  checkArbitrary[OffsetTime]
  checkArbitrary[YearMonth]
  checkArbitrary[ZonedDateTime]

  sealed trait Animal
  final case object Bird extends Animal
  final case object Monkey extends Animal
  case class Food(food: String)

  it should "parse using generic map reader" in {
    val conf = ConfigFactory.parseString("""{"bird": {"food": "worms"}, "monkey": {"food": "banana"}}""")

    implicit val reader = genericMapReader[Animal, Food] {
      case "bird" => Right(Bird)
      case "monkey" => Right(Monkey)
      case animal => Left(UnknownKey(s"$animal is unsupported"))
    }

    conf.to[Map[Animal, Food]].right.value shouldEqual Map(Bird -> Food("worms"), Monkey -> Food("banana"))
  }

  it should "format using generic map writer" in {

    implicit val writer = genericMapWriter[Animal, Food] {
      case Bird => "bird"
      case Monkey => "monkey"
    }

    val config = Map[Animal, Food](Bird -> Food("worms"), Monkey -> Food("banana")).toConfig.render(concise())
    config shouldEqual """{"bird":{"food":"worms"},"monkey":{"food":"banana"}}"""
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
        zoneId <- zoneIdArbitrary.arbitrary.suchThat(_ != ZoneId.of("GMT0")) // Avoid JDK-8138664
      } yield ZonedDateTime.of(localDateTime, zoneId))
}
