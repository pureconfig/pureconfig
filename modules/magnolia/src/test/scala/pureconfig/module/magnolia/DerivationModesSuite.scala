package pureconfig.module.magnolia

import scala.language.higherKinds

import com.typesafe.config.ConfigFactory
import pureconfig._
import shapeless.test.illTyped

class DerivationModesSuite extends BaseSuite {

  sealed trait Entity
  case class Person(name: String, surname: String) extends Entity
  case class Place(name: String, lat: Double, lon: Double) extends Entity

  val person = Person("John", "Doe")
  val conf = ConfigFactory.parseString("{ type: person, name: John, surname: Doe }")

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
    import pureconfig.module.magnolia.reader.semiauto._

    implicit val personReader = deriveReader[Person]
    implicit val placeReader = deriveReader[Place]
    implicit val entityReader = deriveReader[Entity]

    ConfigReader[Entity].from(conf.root) shouldBe Right(person)
  }

  it should "provide methods to derive writers on demand" in {
    import pureconfig.module.magnolia.writer.semiauto._

    implicit val personWriter = deriveWriter[Person]
    implicit val placeWriter = deriveWriter[Place]
    implicit val entityWriter = deriveWriter[Entity]

    ConfigWriter[Entity].to(person) shouldBe conf.root()
  }

  behavior of "auto"

  it should "provide instance derivation for products and coproducts out-of-the-box" in {
    import pureconfig.module.magnolia.reader.auto._
    import pureconfig.module.magnolia.writer.auto._

    ConfigReader[Entity].from(conf.root) shouldBe Right(person)
    ConfigWriter[Entity].to(person) shouldBe conf.root()
  }
}
