package pureconfig.module.magnolia

import scala.language.higherKinds

import com.typesafe.config._
import pureconfig._
import pureconfig.error._
import pureconfig.generic._
import pureconfig.generic.error.{ CoproductHintException, UnexpectedValueForFieldCoproductHint }
import pureconfig.module.magnolia.auto.reader._
import pureconfig.module.magnolia.auto.writer._

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

    it should "fail to read values that are not objects when using a FieldCoproductHint" in {
      val conf = ConfigValueFactory.fromAnyRef("Dog")
      ConfigConvert[AnimalConfig].from(conf) should failWith(
        WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT)))
    }

    it should "fail to read values in the discriminating field that are not strings when using a FieldCoproductHint" in {
      val conf = ConfigFactory.parseString("{ which-animal { type = Dog }, age = 2 }")
      ConfigConvert[AnimalConfig].from(conf.root()) should be(Left(
        ConfigReaderFailures(
          ConvertFailure(WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)), None, "which-animal"))))
    }

    it should "fail with an appropriate reason if an unexpected value is found at the discriminating field when using a FieldCoproductHint" in {
      val conf = ConfigFactory.parseString("{ which-animal = unexpected, age = 2 }")
      ConfigConvert[AnimalConfig].from(conf.root()) should be(Left(
        ConfigReaderFailures(
          ConvertFailure(UnexpectedValueForFieldCoproductHint(ConfigValueFactory.fromAnyRef("unexpected")), None, "which-animal"))))
    }

    it should "fail to read when the hint field conflicts with a field of an option when using a FieldCoproductHint" in {
      sealed trait Conf
      case class AmbiguousConf(typ: String) extends Conf

      implicit val hint = new FieldCoproductHint[Conf]("typ")
      val cc = implicitly[ConfigConvert[Conf]]

      val conf = ConfigFactory.parseString("{ typ = ambiguous-conf }")
      cc.from(conf.root()) should failWithType[KeyNotFound] // "typ" should not be passed to the coproduct option

      val ex = the[CoproductHintException] thrownBy cc.to(AmbiguousConf("ambiguous-conf"))
      ex.failure shouldEqual CollidingKeys("typ", ConfigValueFactory.fromAnyRef("ambiguous-conf"))
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
