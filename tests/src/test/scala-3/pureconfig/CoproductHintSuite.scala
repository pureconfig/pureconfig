package pureconfig

import com.typesafe.config.*

import pureconfig.error.*
import pureconfig.generic.*
import pureconfig.generic.derivation.syntax.*
import pureconfig.generic.error.{CoproductHintException, UnexpectedValueForFieldCoproductHint}
import pureconfig.syntax.*

class CoproductHintSuite extends BaseSuite {
  enum AnimalConfig {
    case DogConfig(age: Int)
    case CatConfig(age: Int)
    case BirdConfig(canFly: Boolean)
  }

  import AnimalConfig.*

  behavior of "CoproductHint"

  {
    given FieldCoproductHint[AnimalConfig] = new FieldCoproductHint[AnimalConfig]("which-animal") {
      override def fieldValue(name: String) = name.dropRight("Config".length)
    }

    it should "read values as expected when using a FieldCoproductHint" in {
      given ConfigConvert[AnimalConfig] = ConfigConvert.derived
      val conf = ConfigFactory.parseString("{ which-animal = Dog, age = 2 }")

      ConfigReader[AnimalConfig].from(conf.root()) shouldEqual Right(DogConfig(2))
    }

    it should "write values as expected when using a FieldCoproductHint" in {

      given ConfigConvert[AnimalConfig] = ConfigConvert.derived

      val conf = ConfigWriter[AnimalConfig].to(DogConfig(2))
      conf shouldBe a[ConfigObject]
      conf.asInstanceOf[ConfigObject].get("which-animal") shouldEqual ConfigValueFactory.fromAnyRef("Dog")
    }

    it should "fail to read values that are not objects when using a FieldCoproductHint" in {
      given ConfigConvert[AnimalConfig] = ConfigConvert.derived
      val conf = ConfigValueFactory.fromAnyRef("Dog")

      ConfigConvert[AnimalConfig].from(conf) should failWith(
        WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT))
      )
    }

    it should "fail to read values in the discriminating field that are not strings when using a FieldCoproductHint" in {
      given ConfigConvert[AnimalConfig] = ConfigConvert.derived
      val conf = ConfigFactory.parseString("{ which-animal { type = Dog }, age = 2 }")

      ConfigConvert[AnimalConfig].from(conf.root()) should be(
        Left(
          ConfigReaderFailures(
            ConvertFailure(
              WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.STRING)),
              stringConfigOrigin(1),
              "which-animal"
            )
          )
        )
      )
    }

    it should "fail with an appropriate reason if an unexpected value is found at the discriminating field when using a FieldCoproductHint" in {
      given ConfigConvert[AnimalConfig] = ConfigConvert.derived
      val conf = ConfigFactory.parseString("{ which-animal = unexpected, age = 2 }")

      ConfigReader[AnimalConfig].from(conf.root()) should be(
        Left(
          ConfigReaderFailures(
            ConvertFailure(
              UnexpectedValueForFieldCoproductHint(ConfigValueFactory.fromAnyRef("unexpected")),
              stringConfigOrigin(1),
              "which-animal"
            )
          )
        )
      )
    }

    it should "fail to read when the hint field conflicts with a field of an option when using a FieldCoproductHint" in {
      sealed trait Conf
      case class AmbiguousConf(typ: String) extends Conf

      given convert: ConfigConvert[Conf] = ConfigConvert.derived

      given FieldCoproductHint[Conf] = FieldCoproductHint("typ")

      val conf = ConfigFactory.parseString("{ typ = ambiguous-conf }")
      convert.from(conf.root()) should failWithReason[KeyNotFound] // "typ" should not be passed to the coproduct option

      val ex = the[CoproductHintException] thrownBy convert.to(AmbiguousConf("ambiguous-conf"))
      ex.failure shouldEqual CollidingKeys("typ", ConfigValueFactory.fromAnyRef("ambiguous-conf"))
    }

    {
      given CoproductHint[AnimalConfig] = FirstSuccessCoproductHint[AnimalConfig]

      it should "read values as expected when using a FirstSuccessCoproductHint" in {
        given ConfigConvert[AnimalConfig] = ConfigConvert.derived
        val conf = ConfigFactory.parseString("{ can-fly = true }")

        ConfigReader[AnimalConfig].from(conf.root()) shouldBe Right(BirdConfig(true))
      }

      it should "write values as expected when using a FirstSuccessCoproductHint" in {
        given ConfigConvert[AnimalConfig] = ConfigConvert.derived
        val conf = ConfigWriter[AnimalConfig].to(DogConfig(2))
        conf shouldBe a[ConfigObject]
        conf.asInstanceOf[ConfigObject].get("which-animal") shouldBe null
      }
    }

    {
      sealed trait A
      case class AA1(a: Int) extends A
      case class AA2(a: String) extends A
      case class EnclosingA(values: Map[String, A])

      it should "fail to read with errors relevant for coproduct derivation when using the default CoproductHint" in {
        given ConfigConvert[A] = ConfigConvert.derived
        given ConfigConvert[EnclosingA] = ConfigConvert.derived

        val conf = ConfigFactory.parseString("""
        {
          values {
            v1 {
              type = "unexpected"
              a = 2
            }
            v2 {
              type = "aa-2"
              a = "val"
            }
            v3 {
              a = 5
            }
          }
        }
      """)

        val exception = intercept[ConfigReaderException[_]] {
          conf.root().toOrThrow[EnclosingA]
        }

        exception.failures.toList.toSet shouldBe Set(
          ConvertFailure(
            UnexpectedValueForFieldCoproductHint(ConfigValueFactory.fromAnyRef("unexpected")),
            stringConfigOrigin(5),
            "values.v1.type"
          ),
          ConvertFailure(KeyNotFound("type", Set()), stringConfigOrigin(12), "values.v3")
        )
      }
    }
  }
}
