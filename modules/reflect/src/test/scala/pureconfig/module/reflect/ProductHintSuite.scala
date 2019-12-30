package pureconfig.module.reflect

import com.typesafe.config.{ConfigFactory, ConfigObject}
import pureconfig._
import pureconfig.error.{ConvertFailure, KeyNotFound, UnknownKey}
import pureconfig.syntax._
import pureconfig.module.reflect.ReflectConfigReaders._
import pureconfig.module.reflect.ReflectConfigWriters._

import scala.collection.JavaConverters._
import scala.language.higherKinds

class ProductHintSuite extends BaseSuite {

  behavior of "ProductHint"

  case class ConfWithCamelCaseInner(thisIsAnInt: Int, thisIsAnotherInt: Int)
  case class ConfWithCamelCase(camelCaseInt: Int, camelCaseString: String, camelCaseConf: ConfWithCamelCaseInner)

  val confWithCamelCase = ConfWithCamelCase(1, "foobar", ConfWithCamelCaseInner(2, 3))

  /** return all the keys in a `ConfigObject` */
  def allKeys(configObject: ConfigObject): Set[String] = {
    configObject.toConfig().entrySet().asScala.flatMap(_.getKey.split('.')).toSet
  }

  it should "read kebab case config keys to camel case fields by default" in {

    val conf = ConfigFactory.parseString("""{
      camel-case-int = 1
      camel-case-string = "bar"
      camel-case-conf {
        this-is-an-int = 3
        this-is-another-int = 10
      }
    }""")

    implicit val readerInner = configReader2(ConfWithCamelCaseInner)
    implicit val reader = configReader3(ConfWithCamelCase)

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "write kebab case config keys from camel case fields by default" in {
    implicit val writerInner = configWriter2((ConfWithCamelCaseInner.unapply _).andThen(_.get))
    implicit val writer = configWriter3((ConfWithCamelCase.unapply _).andThen(_.get))
    val conf = confWithCamelCase.toConfig.asInstanceOf[ConfigObject]
    allKeys(conf) should contain theSameElementsAs Seq(
      "camel-case-int",
      "camel-case-string",
      "camel-case-conf",
      "this-is-an-int",
      "this-is-another-int")
  }

  it should "allow customizing the field mapping through a product hint" in {
    val conf = ConfigFactory.parseString("""{
        A = 2
        B = "two"
      }""").root()

    case class SampleConf(a: Int, b: String)

    {
      implicit val writer = configWriter2((SampleConf.unapply _).andThen(_.get))
      implicit val reader = configReader2(SampleConf)
      // NOTE: behavior differs from pureconfig.generic (only the first error is reported)
      ConfigConvert[SampleConf].from(conf).left.value.toList should contain theSameElementsAs Seq(
        ConvertFailure(KeyNotFound("a", Set("A")), None, ""),
        ConvertFailure(KeyNotFound("b", Set("B")), None, ""))
    }

    {
      implicit val productHint = ReflectProductHint[SampleConf](ConfigFieldMapping(_.toUpperCase))
      implicit val writer = configWriter2((SampleConf.unapply _).andThen(_.get))
      implicit val reader = configReader2(SampleConf)
      ConfigConvert[SampleConf].from(conf) shouldBe Right(SampleConf(2, "two"))
    }
  }

  it should "read camel case config keys to camel case fields when configured to do so" in {

    implicit def productHint[T] = ReflectProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

    val conf = ConfigFactory.parseString("""{
      camelCaseInt = 1
      camelCaseString = "bar"
      camelCaseConf {
        thisIsAnInt = 3
        thisIsAnotherInt = 10
      }
    }""")

    implicit val readerInner = configReader2(ConfWithCamelCaseInner)
    implicit val reader = configReader3(ConfWithCamelCase)

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "write camel case config keys to camel case fields when configured to do so" in {
    implicit def productHint[T] = ReflectProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
    implicit val writerInner = configWriter2((ConfWithCamelCaseInner.unapply _).andThen(_.get))
    implicit val writer = configWriter3((ConfWithCamelCase.unapply _).andThen(_.get))

    val conf = confWithCamelCase.toConfig.asInstanceOf[ConfigObject]
    allKeys(conf) should contain theSameElementsAs Seq(
      "camelCaseInt",
      "camelCaseString",
      "camelCaseConf",
      "thisIsAnInt",
      "thisIsAnotherInt")
  }

  it should "read pascal case config keys to pascal case fields when configured to do so" in {

    implicit def productHint[T] = ReflectProductHint[T](ConfigFieldMapping(CamelCase, PascalCase))

    val conf = ConfigFactory.parseString("""{
      CamelCaseInt = 1
      CamelCaseString = "bar"
      CamelCaseConf {
        ThisIsAnInt = 3
        ThisIsAnotherInt = 10
      }
    }""")

    implicit val readerInner = configReader2(ConfWithCamelCaseInner)
    implicit val reader = configReader3(ConfWithCamelCase)
    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "write pascal case config keys to pascal case fields when configured to do so" in {
    implicit def productHint[T] = ReflectProductHint[T](ConfigFieldMapping(CamelCase, PascalCase))

    implicit val writerInner = configWriter2((ConfWithCamelCaseInner.unapply _).andThen(_.get))
    implicit val writer = configWriter3((ConfWithCamelCase.unapply _).andThen(_.get))

    val conf = ConfWithCamelCase(1, "foobar", ConfWithCamelCaseInner(2, 3)).toConfig.asInstanceOf[ConfigObject]
    allKeys(conf) should contain theSameElementsAs Seq(
      "CamelCaseInt",
      "CamelCaseString",
      "CamelCaseConf",
      "ThisIsAnInt",
      "ThisIsAnotherInt")
  }

  it should "allow customizing the field mapping only for specific types" in {

    implicit val productHint = ReflectProductHint[ConfWithCamelCase](ConfigFieldMapping(CamelCase, CamelCase))

    val conf = ConfigFactory.parseString("""{
      camelCaseInt = 1
      camelCaseString = "bar"
      camelCaseConf {
        this-is-an-int = 3
        this-is-another-int = 10
      }
    }""")

    implicit val readerInner = configReader2(ConfWithCamelCaseInner)
    implicit val reader = configReader3(ConfWithCamelCase)

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

}
