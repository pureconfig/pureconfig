package pureconfig.configurable

import java.time._
import java.time.format.DateTimeFormatter

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions.concise

import pureconfig.arbitrary._
import pureconfig.error.{CannotConvert, UnknownKey}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigConvert, ConfigReader, ConfigWriter}

class ConfigurableSuite extends BaseSuite {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

  behavior of "configurable converters"

  implicit val localTimeInstance: ConfigConvert[LocalTime] = localTimeConfigConvert(DateTimeFormatter.ISO_TIME)
  implicit val localDateInstance: ConfigConvert[LocalDate] = localDateConfigConvert(DateTimeFormatter.ISO_DATE)
  implicit val localDateTimeInstance: ConfigConvert[LocalDateTime] = localDateTimeConfigConvert(
    DateTimeFormatter.ISO_DATE_TIME
  )
  implicit val monthDayInstance: ConfigConvert[MonthDay] = monthDayConfigConvert(DateTimeFormatter.ofPattern("MM-dd"))
  implicit val offsetDateTimeInstance: ConfigConvert[OffsetDateTime] = offsetDateTimeConfigConvert(
    DateTimeFormatter.ISO_OFFSET_DATE_TIME
  )
  implicit val offsetTimeInstance: ConfigConvert[OffsetTime] = offsetTimeConfigConvert(
    DateTimeFormatter.ISO_OFFSET_TIME
  )
  implicit val yearMonthInstance: ConfigConvert[YearMonth] = yearMonthConfigConvert(
    DateTimeFormatter.ofPattern("yyyy-MM")
  )
  implicit val zonedDateTimeInstance: ConfigConvert[ZonedDateTime] = zonedDateTimeConfigConvert(
    DateTimeFormatter.ISO_ZONED_DATE_TIME
  )

  checkArbitrary[LocalTime]
  checkArbitrary[LocalDate]
  checkArbitrary[LocalDateTime]
  checkArbitrary[MonthDay]
  checkArbitrary[OffsetDateTime]
  checkArbitrary[OffsetTime]
  checkArbitrary[YearMonth]
  checkArbitrary[ZonedDateTime]

  sealed trait Animal
  case object Bird extends Animal
  case object Monkey extends Animal
  case class Food(food: String)
  object Food {
    implicit val foodReader: ConfigReader[Food] = ConfigReader.forProduct1("food")(Food.apply)
    implicit val foodWriter: ConfigWriter[Food] = ConfigWriter.forProduct1("food")(_.food)
  }

  it should "parse using generic map reader" in {
    val conf = ConfigFactory.parseString("""{"bird": {"food": "worms"}, "monkey": {"food": "banana"}}""")

    implicit val reader: ConfigReader[Map[Animal, Food]] = genericMapReader[Animal, Food] {
      case "bird" => Right(Bird)
      case "monkey" => Right(Monkey)
      case animal => Left(UnknownKey(s"$animal is unsupported"))
    }

    conf.to[Map[Animal, Food]].value shouldEqual Map(Bird -> Food("worms"), Monkey -> Food("banana"))
  }

  it should "format using generic map writer" in {

    implicit val writer: ConfigWriter[Map[Animal, Food]] = genericMapWriter[Animal, Food] {
      case Bird => "bird"
      case Monkey => "monkey"
    }

    val config = Map[Animal, Food](Bird -> Food("worms"), Monkey -> Food("banana")).toConfig.render(concise())
    config shouldEqual """{"bird":{"food":"worms"},"monkey":{"food":"banana"}}"""
  }
}
