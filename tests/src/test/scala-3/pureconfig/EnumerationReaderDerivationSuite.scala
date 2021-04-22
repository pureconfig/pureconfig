package pureconfig

import scala.compiletime.testing.{typeChecks, typeCheckErrors}
import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigValueFactory, ConfigValueType}
import pureconfig._
import pureconfig.error.WrongType
import pureconfig.generic.{EnumConfigReader, deriveForEnum}
import pureconfig.generic.error.NoValidCoproductOptionFound

enum Color derives EnumConfigReader {
  case RainyBlue, SunnyYellow
}

class EnumerationReaderDerivationSuite extends BaseSuite {

  import Color._

  behavior of "deriveEnumeration"

  it should "provide methods to derive readers for enumerations encoded as sealed traits or enums" in {
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(SunnyYellow)

    val unknownValue = ConfigValueFactory.fromAnyRef("blue")
    ConfigReader[Color]
      .from(unknownValue) should failWith(NoValidCoproductOptionFound(unknownValue, Seq.empty), "", emptyConfigOrigin)

    val conf = ConfigFactory.parseString("{ type: person, name: John, surname: Doe }")
    ConfigReader[Color].from(conf.root()) should failWith(
      WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "provide customizable methods to derive readers for enumerations encoded as sealed traits or enums" in {
    given EnumConfigReader[Color] = deriveForEnum[Color](ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy_blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny_yellow")) shouldBe Right(SunnyYellow)
  }
}
