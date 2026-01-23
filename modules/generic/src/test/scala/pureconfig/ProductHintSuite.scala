package pureconfig

import scala.jdk.CollectionConverters._

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValueType}

import pureconfig.error._
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.syntax._

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

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "write kebab case config keys from camel case fields by default" in {
    val conf = confWithCamelCase.toConfig.asInstanceOf[ConfigObject]
    allKeys(conf) should contain theSameElementsAs Seq(
      "camel-case-int",
      "camel-case-string",
      "camel-case-conf",
      "this-is-an-int",
      "this-is-another-int"
    )
  }

  it should "allow customizing the field mapping through a product hint" in {
    val conf = ConfigFactory
      .parseString("""{
        A = 2
        B = "two"
      }""")
      .root()

    case class SampleConf(a: Int, b: String)
    ConfigConvert[SampleConf].from(conf).left.value.toList should contain theSameElementsAs Seq(
      ConvertFailure(KeyNotFound("a", Set("A")), stringConfigOrigin(1), ""),
      ConvertFailure(KeyNotFound("b", Set("B")), stringConfigOrigin(1), "")
    )

    implicit val productHint = ProductHint[SampleConf](ConfigFieldMapping(_.toUpperCase))
    ConfigConvert[SampleConf].from(conf) shouldBe Right(SampleConf(2, "two"))
  }

  it should "read camel case config keys to camel case fields when configured to do so" in {

    implicit def productHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, CamelCase))

    val conf = ConfigFactory.parseString("""{
      camelCaseInt = 1
      camelCaseString = "bar"
      camelCaseConf {
        thisIsAnInt = 3
        thisIsAnotherInt = 10
      }
    }""")

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "write camel case config keys to camel case fields when configured to do so" in {
    implicit def productHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, CamelCase))

    val conf = confWithCamelCase.toConfig.asInstanceOf[ConfigObject]
    allKeys(conf) should contain theSameElementsAs Seq(
      "camelCaseInt",
      "camelCaseString",
      "camelCaseConf",
      "thisIsAnInt",
      "thisIsAnotherInt"
    )
  }

  it should "read pascal case config keys to pascal case fields when configured to do so" in {

    implicit def productHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, PascalCase))

    val conf = ConfigFactory.parseString("""{
      CamelCaseInt = 1
      CamelCaseString = "bar"
      CamelCaseConf {
        ThisIsAnInt = 3
        ThisIsAnotherInt = 10
      }
    }""")

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "write pascal case config keys to pascal case fields when configured to do so" in {
    implicit def productHint[A] = ProductHint[A](ConfigFieldMapping(CamelCase, PascalCase))

    val conf = ConfWithCamelCase(1, "foobar", ConfWithCamelCaseInner(2, 3)).toConfig.asInstanceOf[ConfigObject]
    allKeys(conf) should contain theSameElementsAs Seq(
      "CamelCaseInt",
      "CamelCaseString",
      "CamelCaseConf",
      "ThisIsAnInt",
      "ThisIsAnotherInt"
    )
  }

  it should "allow customizing the field mapping only for specific types" in {

    implicit val productHint = ProductHint[ConfWithCamelCase](ConfigFieldMapping(CamelCase, CamelCase))

    val conf = ConfigFactory.parseString("""{
      camelCaseInt = 1
      camelCaseString = "bar"
      camelCaseConf {
        this-is-an-int = 3
        this-is-another-int = 10
      }
    }""")

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "disallow unknown keys if specified through a product hint" in {

    case class Conf1(a: Int, c: Int)
    case class Conf2(a: Int, c: Int)

    implicit val productHint = ProductHint[Conf2](allowUnknownKeys = false)

    val conf = ConfigFactory.parseString("""{
      conf {
        a = 1
        b = 2
        c = 3
      }
    }""")

    conf.getConfig("conf").to[Conf1] shouldBe Right(Conf1(1, 3))
    conf.getConfig("conf").to[Conf2] should failWith(UnknownKey("b"), "b", stringConfigOrigin(4))
  }

  it should "disallow unknown keys even for types with no fields" in {
    case class EmptyConf1()
    case class EmptyConf2()
    case object ObjConf1
    case object ObjConf2

    implicit val productHintEmptyConf = ProductHint[EmptyConf2](allowUnknownKeys = false)
    implicit val productHintObjConf = ProductHint[ObjConf2.type](allowUnknownKeys = false)

    val conf = ConfigFactory.parseString("""{
      conf {
        a = 1
      }
    }""")

    conf.getConfig("conf").to[EmptyConf1] shouldBe Right(EmptyConf1())
    conf.getConfig("conf").to[EmptyConf2] should failWith(UnknownKey("a"), "a", stringConfigOrigin(3))
    conf.getConfig("conf").to[ObjConf1.type] shouldBe Right(ObjConf1)
    conf.getConfig("conf").to[ObjConf2.type] should failWith(UnknownKey("a"), "a", stringConfigOrigin(3))
  }

  it should "accumulate all failures if the product hint doesn't allow unknown keys" in {
    case class Conf(a: Int)

    implicit val productHint = ProductHint[Conf](allowUnknownKeys = false)

    val conf = ConfigFactory.parseString("""{
      conf {
        a = "hello"
        b = 1
      }
    }""".stripMargin)

    conf.getConfig("conf").to[Conf] shouldBe Left(
      ConfigReaderFailures(
        ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), stringConfigOrigin(3), "a"),
        ConvertFailure(UnknownKey("b"), stringConfigOrigin(4), "b")
      )
    )
  }

  it should "not use default arguments if specified through a product hint" in {
    case class InnerConf(e: Int, g: Int)
    case class Conf(
        a: Int,
        b: String = "default",
        c: Int = 42,
        d: InnerConf = InnerConf(43, 44),
        e: Option[Int] = Some(45)
    )

    implicit val productHint = ProductHint[Conf](useDefaultArgs = false)

    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava)
    conf1.to[Conf].left.value.toList should contain theSameElementsAs Seq(
      ConvertFailure(KeyNotFound("b"), emptyConfigOrigin, ""),
      ConvertFailure(KeyNotFound("c"), emptyConfigOrigin, ""),
      ConvertFailure(KeyNotFound("d"), emptyConfigOrigin, "")
    )
  }

  it should "include candidate keys in failure reasons in case of a suspected misconfigured ProductHint" in {
    case class CamelCaseConf(camelCaseInt: Int, camelCaseString: String)
    case class KebabCaseConf(kebabCaseInt: Int, kebabCaseString: String)
    case class SnakeCaseConf(snakeCaseInt: Int, snakeCaseString: String)
    case class EnclosingConf(camelCaseConf: CamelCaseConf, kebabCaseConf: KebabCaseConf, snakeCaseConf: SnakeCaseConf)

    val conf = ConfigFactory.parseString("""{
      camel-case-conf {
        camelCaseInt = 2
        camelCaseString = "str"
      }
      kebab-case-conf {
        kebab-case-int = 2
        kebab-case-string = "str"
      }
      snake-case-conf {
        snake_case_int = 2
        snake_case_string = "str"
      }
    }""")

    val exception = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[EnclosingConf]
    }

    exception.failures.toList.toSet shouldBe Set(
      ConvertFailure(KeyNotFound("camel-case-int", Set("camelCaseInt")), stringConfigOrigin(2), "camel-case-conf"),
      ConvertFailure(
        KeyNotFound("camel-case-string", Set("camelCaseString")),
        stringConfigOrigin(2),
        "camel-case-conf"
      ),
      ConvertFailure(KeyNotFound("snake-case-int", Set("snake_case_int")), stringConfigOrigin(10), "snake-case-conf"),
      ConvertFailure(
        KeyNotFound("snake-case-string", Set("snake_case_string")),
        stringConfigOrigin(10),
        "snake-case-conf"
      )
    )
  }
}
