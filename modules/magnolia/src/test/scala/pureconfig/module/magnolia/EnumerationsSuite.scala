package pureconfig.module.magnolia

import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigValueFactory, ConfigValueType}
import pureconfig._
import pureconfig.error.WrongType
import pureconfig.generic.error.NoValidCoproductOptionFound
import pureconfig.module.magnolia.semiauto.reader._
import pureconfig.module.magnolia.semiauto.writer._
import shapeless.test.illTyped

class EnumerationsSuite extends BaseSuite {

  sealed trait Color
  case object RainyBlue extends Color
  case object SunnyYellow extends Color

  val conf = ConfigFactory.parseString("{ type: person, name: John, surname: Doe }")

  behavior of "deriveEnumeration"

  it should "provide methods to derive readers for enumerations encoded as sealed traits" in {
    implicit val colorReader = deriveEnumerationReader[Color]

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(SunnyYellow)

    val unknownValue = ConfigValueFactory.fromAnyRef("blue")
    ConfigReader[Color]
      .from(unknownValue) should failWith(NoValidCoproductOptionFound(unknownValue, Seq.empty), "", emptyConfigOrigin)
    ConfigReader[Color].from(conf.root()) should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "provide methods to derive writers for enumerations encoded as sealed traits" in {
    implicit val colorWriter = deriveEnumerationWriter[Color]

    ConfigWriter[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigWriter[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide customizable methods to derive readers for enumerations encoded as sealed traits" in {
    implicit val colorReader = deriveEnumerationReader[Color](ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy_blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny_yellow")) shouldBe Right(SunnyYellow)
  }

  it should "provide customizable methods to derive writers for enumerations encoded as sealed traits" in {
    implicit val colorWriter = deriveEnumerationWriter[Color](ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigWriter[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy_blue")
    ConfigWriter[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny_yellow")
  }
}
