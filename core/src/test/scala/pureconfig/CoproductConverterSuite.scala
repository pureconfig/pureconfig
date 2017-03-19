package pureconfig

import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ EitherValues, FlatSpec, Matchers }

class CoproductConverterSuite extends FlatSpec with ConfigConvertChecks with Matchers with EitherValues with GeneratorDrivenPropertyChecks {

  behavior of "ConfigConvert"

  //  // ** save and load, coproduct
  //  it should s"be able to save and load ${classOf[AnimalConfig]}" in {
  //    List(DogConfig(12), CatConfig(3), BirdConfig(true)).foreach { expectedConfig =>
  //      saveAndLoadIsIdentity[AnimalConfig](expectedConfig)
  //    }
  //  }
  //
  //  // ** save and load, coproduct
  //  it should s"read and write disambiguation information on sealed families by default" in {
  //    withTempFile { configFile =>
  //      val conf = ConfigFactory.parseString("{ type = dogconfig, age = 2 }")
  //      loadConfig[AnimalConfig](conf) should be(Right(DogConfig(2)))
  //
  //      saveConfigAsPropertyFile[AnimalConfig](DogConfig(2), configFile, overrideOutputPath = true)
  //      loadConfig[TypesafeConfig](configFile).right.map(_.getString("type")) should be(Right("dogconfig"))
  //    }
  //  }
  //
  //  // save and load, coproduct
  //  it should s"allow using different strategies for disambiguating between options in a sealed family" in {
  //    withTempFile { configFile =>
  //      implicit val hint = new FieldCoproductHint[AnimalConfig]("which-animal") {
  //        override def fieldValue(name: String) = name.dropRight("Config".length)
  //      }
  //
  //      val conf = ConfigFactory.parseString("{ which-animal = Dog, age = 2 }")
  //      loadConfig[AnimalConfig](conf) should be(Right(DogConfig(2)))
  //
  //      saveConfigAsPropertyFile[AnimalConfig](DogConfig(2), configFile, overrideOutputPath = true)
  //      loadConfig[TypesafeConfig](configFile).right.map(_.getString("which-animal")) should be(Right("Dog"))
  //    }
  //
  //    withTempFile { configFile =>
  //      implicit val hint = new FirstSuccessCoproductHint[AnimalConfig]
  //
  //      val conf = ConfigFactory.parseString("{ can-fly = true }")
  //      loadConfig[AnimalConfig](conf) should be(Right(BirdConfig(true)))
  //
  //      saveConfigAsPropertyFile[AnimalConfig](DogConfig(2), configFile, overrideOutputPath = true)
  //      loadConfig[TypesafeConfig](configFile).right.map(_.hasPath("type")) should be(Right(false))
  //    }
  //  }
  //
  //  // save and load, coproduct
  //  it should "throw an exception if a coproduct option has a field with the same key as the hint field" in {
  //    implicit val hint = new FieldCoproductHint[AnimalConfig]("age")
  //    val cc = implicitly[ConfigConvert[AnimalConfig]]
  //    a[ConfigReaderException[_]] should be thrownBy cc.to(DogConfig(2))
  //  }
  //
  //  // save and load, coproduct
  //  it should "return a Failure with a proper exception if the hint field in a coproduct is missing" in {
  //    val conf = ConfigFactory.parseString("{ can-fly = true }")
  //    val failures = loadConfig[AnimalConfig](conf).left.value.toList
  //    failures should have size 1
  //    failures.head shouldBe a[KeyNotFound]
  //  }
  //
  //  // save and load, coproduct
  //  it should "return a Failure with a proper exception when a coproduct config is missing" in {
  //    case class AnimalCage(animal: AnimalConfig)
  //    val failures = loadConfig[AnimalCage](ConfigFactory.empty()).left.value.toList
  //    failures should have size 1
  //    failures.head shouldBe a[KeyNotFound]
  //  }

}
