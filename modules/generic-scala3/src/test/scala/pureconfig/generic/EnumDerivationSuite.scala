package pureconfig
package generic

import scala.compiletime.testing.{typeCheckErrors, typeChecks}
import scala.deriving.Mirror
import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigValueFactory, ConfigValueType}

import pureconfig._
import pureconfig.error.{CannotConvert, WrongType}
import pureconfig.generic.semiauto._

class EnumDerivationSuite extends BaseSuite {
  behavior of "EnumDerivation"

  it should "provide util methods to derive readers" in {
    enum Color {
      case RainyBlue, SunnyYellow
    }

    given ConfigReader[Color] = deriveEnumerationReader

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(Color.RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(Color.SunnyYellow)

    val unknownValue = ConfigValueFactory.fromAnyRef("blue")

    ConfigReader[Color].from(unknownValue) should failWith(
      CannotConvert("blue", "Color", "The value is not a valid enum option."),
      "",
      emptyConfigOrigin
    )

    val conf = ConfigFactory.parseString("{ type: person, name: John, surname: Doe }")

    ConfigReader[Color].from(conf.root()) should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "provide util methods to derive writers for enumerations encoded as enums or sealed traits" in {
    enum Color {
      case RainyBlue, SunnyYellow
    }

    given ConfigWriter[Color] = deriveEnumerationWriter

    ConfigWriter[Color].to(Color.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigWriter[Color].to(Color.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide util methods to derive full converters for enumerations encoded as enums or sealed traits" in {
    enum Color {
      case RainyBlue, SunnyYellow
    }

    given ConfigConvert[Color] = deriveEnumerationConvert

    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(Color.RainyBlue)
    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(Color.SunnyYellow)
    ConfigConvert[Color].to(Color.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigConvert[Color].to(Color.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide customizable util methods to derive readers" in {
    enum Color {
      case RainyBlue, SunnyYellow
    }

    given ConfigReader[Color] = deriveEnumerationReader(ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy_blue")) shouldBe Right(Color.RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny_yellow")) shouldBe Right(Color.SunnyYellow)
  }

  it should "provide customizable util methods to derive writers" in {
    enum Color {
      case RainyBlue, SunnyYellow
    }

    given ConfigWriter[Color] = deriveEnumerationWriter(ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigWriter[Color].to(Color.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy_blue")
    ConfigWriter[Color].to(Color.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny_yellow")
  }

  it should "provide customizable util methods to derive converters" in {
    enum Color {
      case RainyBlue, SunnyYellow
    }

    given ConfigConvert[Color] = deriveEnumerationConvert(ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy_blue")) shouldBe Right(Color.RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny_yellow")) shouldBe Right(Color.SunnyYellow)
    ConfigWriter[Color].to(Color.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy_blue")
    ConfigWriter[Color].to(Color.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny_yellow")
  }

}
