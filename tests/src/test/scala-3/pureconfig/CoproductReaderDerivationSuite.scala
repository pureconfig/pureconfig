package pureconfig

import scala.language.higherKinds

import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.error._
import pureconfig.generic._
import pureconfig.generic.derivation.default.{derived, withProductHint, withCoproductHint}

enum AnimalConfig derives ConfigReader {
  case DogConfig(age: Int)
  case CatConfig(age: Int)
  case BirdConfig(canFly: Boolean)
}

class CoproductReaderDerivationSuite extends BaseSuite {

  import AnimalConfig._

  behavior of "ConfigConvert"

  it should "read disambiguation information on sealed families by default" in {
    val conf = ConfigFactory.parseString("{ type = dog-config, age = 2 }")
    ConfigReader[AnimalConfig].from(conf.root()) shouldEqual Right(DogConfig(2))
  }

  it should "return a proper ConfigReaderFailure if the hint field in a coproduct is missing" in {
    val conf = ConfigFactory.parseString("{ can-fly = true }")
    ConfigReader[AnimalConfig].from(conf.root()) should failWithReason[KeyNotFound]
  }

  it should "return a proper ConfigReaderFailure when a coproduct config is missing" in {
    case class AnimalCage(animal: AnimalConfig) derives ConfigReader
    ConfigReader[AnimalCage].from(ConfigFactory.empty().root()) should failWithReason[KeyNotFound]
  }

  it should "be able to use custom derived readers" in {
    val Snake =
      withProductHint(
        ProductHint(ConfigFieldMapping(CamelCase, SnakeCase))
      )

    val Kebab =
      withCoproductHint(
        FieldCoproductHint("variant")
      )

    type SnakeConfigReader[A] = Snake.DerivedConfigReader[A]
    type KebabConfigReader[A] = Kebab.DerivedConfigReader[A]

    case class Baggage(hairAccessoriesCount: Int) derives Snake.DerivedConfigReader

    enum VehicleConfig derives KebabConfigReader {
      case Car(wheels: Int, baggageContent: Baggage)
      case Bike(wheels: Int)
    }

    import VehicleConfig._

    val conf = ConfigFactory.parseString(
      "{ variant = car, wheels = 4, baggage-content = { hair_accessories_count = 5 } }"
    )
    ConfigReader[VehicleConfig].from(conf.root()) shouldEqual Right(Car(4, Baggage(5)))
  }
}
