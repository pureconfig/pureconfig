package pureconfig

import scala.compiletime.testing.{typeCheckErrors, typeChecks}
import scala.deriving.Mirror
import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigValueFactory, ConfigValueType}

import pureconfig._
import pureconfig.error.{CannotConvert, WrongType}
import pureconfig.generic.derivation.{EnumConfigReader, EnumConfigReaderDerivation}

enum Color derives EnumConfigReader {
  case RainyBlue, SunnyYellow
}

class EnumerationReaderDerivationSuite extends BaseSuite {

  import Color._

  behavior of "EnumConfigReader"

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
}
