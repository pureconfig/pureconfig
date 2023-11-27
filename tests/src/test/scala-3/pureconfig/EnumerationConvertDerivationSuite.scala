package pureconfig

import scala.compiletime.testing.{typeCheckErrors, typeChecks}
import scala.deriving.Mirror
import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigValueFactory, ConfigValueType}

import pureconfig.*
import pureconfig.error.{CannotConvert, WrongType}
import pureconfig.generic.derivation.defaults.given
import pureconfig.generic.derivation.{EnumConfigConvert, EnumConfigReader, EnumConfigWriter, EnumHint}

enum Color derives EnumConfigReader {
  case RainyBlue, SunnyYellow
}

class EnumerationConvertDerivationSuite extends BaseSuite {

  import Color.*

  behavior of "EnumConfigConvert"

  it should "provide methods to derive readers for enumerations encoded as sealed traits or enums" in {
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(SunnyYellow)

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

  it should "provide methods to derive writers for enumerations encoded as sealed traits" in {
    given ConfigWriter[Color] = EnumConfigWriter.derived

    ConfigWriter[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigWriter[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide methods to derive full converters for enumerations encoded as sealed traits" in {
    sealed trait Color
    case object RainyBlue extends Color
    case object SunnyYellow extends Color

    given ConfigConvert[Color] = EnumConfigConvert.derived

    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(RainyBlue)
    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(SunnyYellow)
    ConfigConvert[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigConvert[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide customizable methods to derive readers for enumerations encoded as sealed traits" in {
    given EnumConfigReader[Color] = EnumConfigReader.deriveEnumerationReader(ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy_blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny_yellow")) shouldBe Right(SunnyYellow)
  }

  it should "provide customizable methods to derive writers for enumerations encoded as sealed traits" in {
    given EnumHint[Color] = EnumHint(ConfigFieldMapping(PascalCase, SnakeCase))
    given EnumConfigWriter[Color] = EnumConfigWriter.derived

    ConfigWriter[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy_blue")
    ConfigWriter[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny_yellow")
  }
}
