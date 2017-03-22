package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigValueFactory }
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ EitherValues, FlatSpec, Matchers }
import pureconfig.error.ConfigReaderException

class CoproductHintSuite extends FlatSpec with ConfigConvertChecks with Matchers with EitherValues with GeneratorDrivenPropertyChecks {

  behavior of "CoproductHint"

  {
    implicit val hint = new FieldCoproductHint[AnimalConfig]("which-animal") {
      override def fieldValue(name: String) = name.dropRight("Config".length)
    }

    it should "allow using different strategies for disambiguating between options in a sealed family when reading" in {
      val conf = ConfigFactory.parseString("{ which-animal = Dog, age = 2 }")
      ConfigConvert[AnimalConfig].from(conf.root()) shouldEqual Right(DogConfig(2))
    }

    it should "allow using different strategies for disambiguating between options in a sealed family when writing" in {
      val conf = ConfigConvert[AnimalConfig].to(DogConfig(2))
      conf shouldBe a[ConfigObject]
      conf.asInstanceOf[ConfigObject].get("which-animal") shouldEqual ConfigValueFactory.fromAnyRef("Dog")
    }
  }

  {
    implicit val hint = new FirstSuccessCoproductHint[AnimalConfig]

    it should "allow to override the strategy for disambiguating between options in a sealed family when reading" in {
      val conf = ConfigFactory.parseString("{ can-fly = true }")
      ConfigConvert[AnimalConfig].from(conf.root()) shouldBe Right(BirdConfig(true))
    }

    it should "allow to override the strategy for disambiguating between options in a sealed family when writing" in {
      val conf = ConfigConvert[AnimalConfig].to(DogConfig(2))
      conf shouldBe a[ConfigObject]
      conf.asInstanceOf[ConfigObject].get("which-animal") shouldBe null
    }
  }

  it should "throw an exception if a coproduct option has a field with the same key as the hint field" in {
    implicit val hint = new FieldCoproductHint[AnimalConfig]("age")
    val cc = implicitly[ConfigConvert[AnimalConfig]]
    a[ConfigReaderException[_]] should be thrownBy cc.to(DogConfig(2))
  }

}
