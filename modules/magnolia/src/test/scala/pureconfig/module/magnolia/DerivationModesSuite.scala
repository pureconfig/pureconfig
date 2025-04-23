package pureconfig.module.magnolia

import com.typesafe.config.ConfigFactory
import shapeless.test.illTyped

import pureconfig._

class DerivationModesSuite extends BaseSuite {

  sealed trait Entity
  case class Person(name: String, surname: String) extends Entity
  case class Place(name: String, lat: Double, lon: Double) extends Entity

  val person = Person("John", "Doe")
  val conf = ConfigFactory.parseString("{ type: person, name: John, surname: Doe }")

  case class CustomCaseClass(
      customObject: CustomObject,
      mapCustomObject: Map[String, CustomObject],
      mapListCustomObject: Map[String, List[CustomObject]]
  )
  case class CustomObject(value: Int)
  object CustomObject {
    implicit val pureconfigReader: ConfigReader[CustomObject] = ConfigReader.fromStringOpt {
      case "eaaxacaca" => Some(CustomObject(453))
      case "a" => Some(CustomObject(45))
      case _ => Some(CustomObject(1))
    }
    implicit val pureconfigWriter: ConfigWriter[CustomObject] = ConfigWriter.toString {
      case CustomObject(453) => "eaaxacaca"
      case CustomObject(45) => "a"
      case _ => "cvbc"
    }
  }

  val customCaseClass = CustomCaseClass(
    CustomObject(453),
    Map("a" -> CustomObject(453), "b" -> CustomObject(45)),
    Map("x" -> List(CustomObject(45), CustomObject(453), CustomObject(1)))
  )
  val customConf = ConfigFactory.parseString("""{
      |  custom-object = "eaaxacaca"
      |  map-custom-object { a = "eaaxacaca", b = "a" }
      |  map-list-custom-object { x = ["a", "eaaxacaca", "cvbc"]}
      |}""".stripMargin)

  behavior of "default"

  it should "not provide instance derivation for products and coproducts out-of-the-box" in {
    illTyped("loadConfig[Entity](conf)")
    illTyped("ConfigWriter[Entity]")
  }

  behavior of "semiauto"

  it should "not provide instance derivation for products and coproducts out-of-the-box" in {
    illTyped("{ import pureconfig.module.magnolia.reader.semiauto._; loadConfig[Entity](conf) }")
    illTyped("{ import pureconfig.module.magnolia.reader.semiauto._; ConfigWriter[Entity] }")
  }

  it should "provide methods to derive readers on demand" in {
    import pureconfig.module.magnolia.semiauto.reader._

    implicit val personReader: ConfigReader[Person] = deriveReader[Person]
    implicit val placeReader: ConfigReader[Place] = deriveReader[Place]
    implicit val entityReader: ConfigReader[Entity] = deriveReader[Entity]

    ConfigReader[Entity].from(conf.root) shouldBe Right(person)
  }

  it should "provide methods to derive writers on demand" in {
    import pureconfig.module.magnolia.semiauto.writer._

    implicit val personWriter: ConfigWriter[Person] = deriveWriter[Person]
    implicit val placeWriter: ConfigWriter[Place] = deriveWriter[Place]
    implicit val entityWriter: ConfigWriter[Entity] = deriveWriter[Entity]

    ConfigWriter[Entity].to(person) shouldBe conf.root()
  }

  behavior of "auto"

  it should "provide instance derivation for products and coproducts out-of-the-box" in {
    import pureconfig.module.magnolia.auto.reader._
    import pureconfig.module.magnolia.auto.writer._

    ConfigReader[Entity].from(conf.root) shouldBe Right(person)
    ConfigWriter[Entity].to(person) shouldBe conf.root()
  }

  it should "use existing reader and writer instances when they exist" in {
    import pureconfig.module.magnolia.auto.reader._
    import pureconfig.module.magnolia.auto.writer._

    ConfigReader[CustomCaseClass].from(customConf.root) shouldBe Right(customCaseClass)
    ConfigWriter[CustomCaseClass].to(customCaseClass) shouldBe customConf.root()
  }
}
