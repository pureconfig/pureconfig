package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigValueFactory, ConfigValueType }
import pureconfig.DerivationModesSuite._
import pureconfig.error.WrongType
import pureconfig.generic.error.NoValidCoproductChoiceFound
import shapeless.test.illTyped

object DerivationModesSuite {

  sealed trait Entity
  case class Person(name: String, surname: String) extends Entity
  case class Place(name: String, lat: Double, lon: Double) extends Entity

  val person = Person("John", "Doe")
  val conf = ConfigFactory.parseString("{ type: person, name: John, surname: Doe }")

  sealed trait Color
  case object RainyBlue extends Color
  case object SunnyYellow extends Color
}

class DerivationModesSuite extends BaseSuite {

  behavior of "default"

  it should "not provide instance derivation for products and coproducts out-of-the-box" in {
    illTyped("loadConfig[Entity](conf)")
    illTyped("ConfigWriter[Entity]")
  }

  behavior of "semiauto"

  it should "provide methods to derive readers on demand" in {
    import pureconfig.generic.semiauto._

    implicit val personReader = deriveReader[Person]
    implicit val placeReader = deriveReader[Place]
    implicit val entityReader = deriveReader[Entity]

    loadConfig[Entity](conf) shouldBe Right(person)
  }

  it should "provide methods to derive writers on demand" in {
    import pureconfig.generic.semiauto._

    implicit val personWriter = deriveWriter[Person]
    implicit val placeWriter = deriveWriter[Place]
    implicit val entityWriter = deriveWriter[Entity]

    ConfigWriter[Entity].to(person) shouldBe conf.root()
  }

  it should "provide methods to derive full converters on demand" in {
    import pureconfig.generic.semiauto._

    implicit val personConvert = deriveConvert[Person]
    implicit val placeConvert = deriveConvert[Place]
    implicit val entityConvert = deriveConvert[Entity]

    loadConfig[Entity](conf) shouldBe Right(person)
    ConfigWriter[Entity].to(person) shouldBe conf.root()
  }

  it should "provide methods to derive readers for enumerations encoded as sealed traits" in {
    import pureconfig.generic.semiauto._

    implicit val colorReader = deriveEnumerationReader[Color]

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(SunnyYellow)

    val unknownValue = ConfigValueFactory.fromAnyRef("blue")
    ConfigReader[Color].from(unknownValue) should failWith(NoValidCoproductChoiceFound(unknownValue), "")
    ConfigReader[Color].from(conf.root()) should failWith(WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)), "")
  }

  it should "provide methods to derive writers for enumerations encoded as sealed traits" in {
    import pureconfig.generic.semiauto._

    implicit val colorWriter = deriveEnumerationWriter[Color]

    ConfigWriter[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigWriter[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide methods to derive full converters for enumerations encoded as sealed traits" in {
    import pureconfig.generic.semiauto._

    implicit val colorConvert = deriveEnumerationConvert[Color]

    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("rainy-blue")) shouldBe Right(RainyBlue)
    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("sunny-yellow")) shouldBe Right(SunnyYellow)
    ConfigConvert[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy-blue")
    ConfigConvert[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
  }

  it should "provide customizable methods to derive readers for enumerations encoded as sealed traits" in {
    import pureconfig.generic.semiauto._

    implicit val colorReader = deriveEnumerationReader[Color](ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("rainy_blue")) shouldBe Right(RainyBlue)
    ConfigReader[Color].from(ConfigValueFactory.fromAnyRef("sunny_yellow")) shouldBe Right(SunnyYellow)
  }

  it should "provide customizable methods to derive writers for enumerations encoded as sealed traits" in {
    import pureconfig.generic.semiauto._

    implicit val colorWriter = deriveEnumerationWriter[Color](ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigWriter[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy_blue")
    ConfigWriter[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny_yellow")
  }

  it should "provide customizable methods to derive full converters for enumerations encoded as sealed traits" in {
    import pureconfig.generic.semiauto._

    implicit val colorConvert = deriveEnumerationConvert[Color](ConfigFieldMapping(PascalCase, SnakeCase))

    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("rainy_blue")) shouldBe Right(RainyBlue)
    ConfigConvert[Color].from(ConfigValueFactory.fromAnyRef("sunny_yellow")) shouldBe Right(SunnyYellow)
    ConfigConvert[Color].to(RainyBlue) shouldEqual ConfigValueFactory.fromAnyRef("rainy_blue")
    ConfigConvert[Color].to(SunnyYellow) shouldEqual ConfigValueFactory.fromAnyRef("sunny_yellow")
  }

  it should "not allow deriving readers, writers and full converters for enumerations encoded as sealed traits whose subclasses are not all case objects" in {
    import pureconfig.generic.semiauto._

    illTyped("deriveEnumerationReader[Entity]")
    illTyped("deriveEnumerationWriter[Entity]")
    illTyped("deriveEnumerationConvert[Entity]")
  }

  behavior of "auto"

  it should "provide instance derivation for products and coproducts out-of-the-box" in {
    import pureconfig.generic.auto._

    loadConfig[Entity](conf) shouldBe Right(person)
    ConfigWriter[Entity].to(person) shouldBe conf.root()
  }
}
