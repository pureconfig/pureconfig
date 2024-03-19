package pureconfig

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValueFactory}

import pureconfig.*
import pureconfig.error.*
import pureconfig.error.ConvertFailure as ConfigReaderConvertFailure
import pureconfig.generic.*
import pureconfig.generic.error.UnexpectedValueForFieldCoproductHint
import pureconfig.generic.semiauto.deriveReader

class CoproductReaderDerivationSuite extends BaseSuite {
  enum AnimalConfig {
    case DogConfig(age: Int)
    case CatConfig(age: Int)
    case BirdConfig(canFly: Boolean)
  }

  given ConfigReader[AnimalConfig] = deriveReader

  import AnimalConfig.*

  behavior of "ConfigConvert"

  it should "read disambiguation information on sealed families by default" in {
    val conf = ConfigFactory.parseString("{ type = dog-config, age = 2 }")
    ConfigReader[AnimalConfig].from(conf.root()) shouldEqual Right(DogConfig(2))
  }

  // it should "write disambiguation information on sealed families by default" in {
  //   val conf = ConfigWriter[AnimalConfig].to(DogConfig(2))

  //   conf shouldBe a[ConfigObject]
  //   conf.asInstanceOf[ConfigObject].get("type") shouldEqual ConfigValueFactory.fromAnyRef("dog-config")
  // }

  it should "return a proper ConfigReaderFailure if the hint field in a coproduct is missing" in {
    val conf = ConfigFactory.parseString("{ can-fly = true }")
    ConfigReader[AnimalConfig].from(conf.root()) should failWithReason[KeyNotFound]
  }

  it should "return a proper ConfigReaderFailure if the hint field in a coproduct contains an invalid option" in {
    val conf = ConfigFactory.parseString("{ can-fly = true, type = car-config }")
    val expectedFailure = ConfigReaderConvertFailure(
      UnexpectedValueForFieldCoproductHint(ConfigValueFactory.fromAnyRef("car-config")),
      stringConfigOrigin(1),
      "type"
    )

    ConfigReader[AnimalConfig].from(conf.root()) should failWith(expectedFailure)
  }

  it should "return a proper ConfigReaderFailure when a coproduct config is missing" in {
    case class AnimalCage(animal: AnimalConfig)
    given ConfigReader[AnimalCage] = deriveReader

    ConfigReader[AnimalCage].from(ConfigFactory.empty().root()) should failWithReason[KeyNotFound]
  }

}
