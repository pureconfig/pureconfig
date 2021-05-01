package pureconfig

import scala.language.higherKinds

import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.error._
import pureconfig.generic.derivation._

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
}
