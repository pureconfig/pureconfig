package pureconfig.module.magnolia

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValueFactory}
import org.scalacheck.{Arbitrary, Gen}

import pureconfig._
import pureconfig.error._
import pureconfig.module.magnolia.auto.reader._
import pureconfig.module.magnolia.auto.writer._

sealed trait CarMaker

object CarMaker {
  case object Mercedes extends CarMaker
  case object BMW extends CarMaker
  case object Tesla extends CarMaker
}

class CoproductConvertersSuite extends BaseSuite {

  behavior of "ConfigConvert"

  val genBirdConfig: Gen[BirdConfig] = Arbitrary.arbBool.arbitrary.map(BirdConfig.apply)
  val genCatConfig: Gen[CatConfig] = Arbitrary.arbInt.arbitrary.map(CatConfig.apply)
  val genDogConfig: Gen[DogConfig] = Arbitrary.arbInt.arbitrary.map(DogConfig.apply)
  val genAnimalConfig: Gen[AnimalConfig] = Gen.oneOf(genBirdConfig, genCatConfig, genDogConfig)
  implicit val arbAnimalConfig: Arbitrary[AnimalConfig] = Arbitrary(genAnimalConfig)

  checkArbitrary[AnimalConfig]

  it should "read disambiguation information on sealed families by default" in {
    val conf = ConfigFactory.parseString("{ type = dog-config, age = 2 }")
    ConfigConvert[AnimalConfig].from(conf.root()) shouldEqual Right(DogConfig(2))
  }

  it should "write disambiguation information on sealed families by default" in {
    val conf = ConfigConvert[AnimalConfig].to(DogConfig(2))
    conf shouldBe a[ConfigObject]
    conf.asInstanceOf[ConfigObject].get("type") shouldEqual ConfigValueFactory.fromAnyRef("dog-config")
  }

  it should "return a proper ConfigReaderFailure if the hint field in a coproduct is missing" in {
    val conf = ConfigFactory.parseString("{ can-fly = true }")
    ConfigConvert[AnimalConfig].from(conf.root()) should failWithReason[KeyNotFound]
  }

  it should "return a proper ConfigReaderFailure when a coproduct config is missing" in {
    case class AnimalCage(animal: AnimalConfig)
    ConfigConvert[AnimalCage].from(ConfigFactory.empty().root()) should failWithReason[KeyNotFound]
  }

  it should "read disambiguation information on sealed families with the cases nested in the companion" in {
    import CarMaker._
    val conf = ConfigFactory.parseString("{ type = bmw }")
    ConfigConvert[CarMaker].from(conf.root()) shouldEqual Right(BMW)
  }
}
