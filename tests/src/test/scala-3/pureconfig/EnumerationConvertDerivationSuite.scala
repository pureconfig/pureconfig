package pureconfig

import scala.compiletime.testing.{typeCheckErrors, typeChecks}
import scala.deriving.Mirror
import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigValueFactory, ConfigValueType}

import pureconfig._
import pureconfig.error.{CannotConvert, WrongType}
import pureconfig.generic.derivation._

class EnumerationConvertDerivationSuite extends BaseSuite {
  behavior of "EnumConfigConvert"

  it should "provide methods to derive readers for enumerations encoded as enums or sealed traits" in {
    enum Color derives EnumConfigReader {
      case RainyBlue, SunnyYellow
    }

    sealed trait Color2 derives EnumConfigReader
    object Color2 {
      case object RainyBlue extends Color2
      case object SunnyYellow extends Color2
    }

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(Color.RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(Color.SunnyYellow)

    ConfigReader[Color2].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(Color2.RainyBlue)
    ConfigReader[Color2].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(Color2.SunnyYellow)

    val unknownValue = ConfigValueFactory.fromAnyRef("blue")

    ConfigReader[Color].from(unknownValue) should failWith(
      CannotConvert("blue", "Color", "The value is not a valid enum option."),
      "",
      emptyConfigOrigin
    )

    ConfigReader[Color2].from(unknownValue) should failWith(
      CannotConvert("blue", "Color2", "The value is not a valid enum option."),
      "",
      emptyConfigOrigin
    )

    val conf = ConfigFactory.parseString("{ type: person, name: John, surname: Doe }")

    ConfigReader[Color].from(conf.root()) should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)),
      "",
      stringConfigOrigin(1)
    )

    ConfigReader[Color2].from(conf.root()) should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "provide methods to derive writers for enumerations encoded as enums or sealed traits" in {
    enum Color derives EnumConfigWriter {
      case RainyBlue, SunnyYellow
    }

    sealed trait Color2 derives EnumConfigWriter
    object Color2 {
      case object RainyBlue extends Color2
      case object SunnyYellow extends Color2
    }

    ConfigWriter[Color].to(Color.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigWriter[Color].to(Color.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")

    ConfigWriter[Color2].to(Color2.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigWriter[Color2].to(Color2.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide methods to derive full converters for enumerations encoded as enums or sealed traits" in {
    enum Color derives EnumConfigConvert {
      case RainyBlue, SunnyYellow
    }

    sealed trait Color2 derives EnumConfigConvert
    object Color2 {
      case object RainyBlue extends Color2
      case object SunnyYellow extends Color2
    }

    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(Color.RainyBlue)
    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(Color.SunnyYellow)
    ConfigConvert[Color].to(Color.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigConvert[Color].to(Color.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")

    ConfigConvert[Color2].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(Color2.RainyBlue)
    ConfigConvert[Color2].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(Color2.SunnyYellow)
    ConfigConvert[Color2].to(Color2.RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigConvert[Color2].to(Color2.SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }
}
