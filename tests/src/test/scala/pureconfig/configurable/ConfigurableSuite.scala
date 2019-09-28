package pureconfig.configurable

import java.time._
import java.time.format.DateTimeFormatter

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions.concise
import pureconfig.BaseSuite
import pureconfig.arbitrary._
import pureconfig.error.UnknownKey
import pureconfig.generic.auto._
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
