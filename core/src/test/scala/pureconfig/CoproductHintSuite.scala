package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigValueFactory, ConfigValueType }
import pureconfig.error.{ ConfigReaderException, ConfigReaderFailures, WrongType }

class CoproductHintSuite extends BaseSuite {

  behavior of "CoproductHint"

  {
    implicit val hint = new FieldCoproductHint[AnimalConfig]("which-animal") {
      override def fieldValue(name: String) = name.dropRight("Config".length)
    }

    it should "read values as expected when using a FieldCoproductHint" in {
      val conf = ConfigFactory.parseString("{ which-animal = Dog, age = 2 }")
      ConfigConvert[AnimalConfig].from(conf.root()) shouldEqual Right(DogConfig(2))
    }

    it should "write values as expected when using a FieldCoproductHint" in {
      val conf = ConfigConvert[AnimalConfig].to(DogConfig(2))
      conf shouldBe a[ConfigObject]
      conf.asInstanceOf[ConfigObject].get("which-animal") shouldEqual ConfigValueFactory.fromAnyRef("Dog")
    }

    it should "throw an exception when the hint field conflicts with a field of an option when using a FieldCoproductHint" in {
      implicit val hint = new FieldCoproductHint[AnimalConfig]("age")
      val cc = implicitly[ConfigConvert[AnimalConfig]]
      a[ConfigReaderException[_]] should be thrownBy cc.to(DogConfig(2))
    }
  }

  {
    sealed trait Color
    case object RainyBlue extends Color
    case object SunnyYellow extends Color

    implicit val hint = new EnumCoproductHint[Color] {
      override def fieldValue(name: String) = ConfigFieldMapping(CamelCase, KebabCase)(name)
    }

    implicit val badHint = new EnumCoproductHint[AnimalConfig]

    it should "read values as expected when using an EnumCoproductHint" in {
      val conf = ConfigValueFactory.fromAnyRef("rainy-blue")
      ConfigConvert[Color].from(conf) shouldEqual Right(RainyBlue)
    }

    it should "write values as expected when using an EnumCoproductHint" in {
      val conf = ConfigConvert[Color].to(SunnyYellow)
      conf shouldEqual ConfigValueFactory.fromAnyRef("sunny-yellow")
    }

    it should "fail to read values that are not case objects when using an EnumCoproductHint" in {
      val conf = ConfigFactory.parseString("{ which-animal = Dog, age = 2 }")
      ConfigConvert[AnimalConfig].from(conf.root()) shouldEqual Left(ConfigReaderFailures(
        WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING), None, None)))
    }

    it should "fail to write values that are not case objects when using an EnumCoproductHint" in {
      a[ConfigReaderException[_]] should be thrownBy ConfigConvert[AnimalConfig].to(DogConfig(2))
    }
  }

  {
    implicit val hint = new FirstSuccessCoproductHint[AnimalConfig]

    it should "read values as expected when using a FirstSuccessCoproductHint" in {
      val conf = ConfigFactory.parseString("{ can-fly = true }")
      ConfigConvert[AnimalConfig].from(conf.root()) shouldBe Right(BirdConfig(true))
    }

    it should "write values as expected when using a FirstSuccessCoproductHint" in {
      val conf = ConfigConvert[AnimalConfig].to(DogConfig(2))
      conf shouldBe a[ConfigObject]
      conf.asInstanceOf[ConfigObject].get("which-animal") shouldBe null
    }
  }
}
