package pureconfig

import scala.collection.JavaConverters.*

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValueType}

import pureconfig.error.*
import pureconfig.generic.ProductHint
import pureconfig.generic.semiauto.deriveReader
import pureconfig.syntax.*

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
    given ConfigReader[ConfWithCamelCase] = deriveReader

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

  it should "allow customizing the field mapping through a product hint" in {
    val conf = ConfigFactory
      .parseString("""{
        A = 2
        B = "two"
      }""")
      .root()

    case class SampleConf(a: Int, b: String)

    val default = deriveReader[SampleConf]
    val customized =
      given ProductHint[SampleConf] = ProductHint(ConfigFieldMapping(_.toUpperCase))
      deriveReader[SampleConf]

    ConfigReader[SampleConf](using default).from(conf).left.value.toList should contain theSameElementsAs Seq(
      ConvertFailure(KeyNotFound("a", Set("A")), stringConfigOrigin(1), ""),
      ConvertFailure(KeyNotFound("b", Set("B")), stringConfigOrigin(1), "")
    )

    ConfigReader[SampleConf](using customized).from(conf) shouldBe Right(SampleConf(2, "two"))
  }

  it should "read camel case config keys to camel case fields when configured to do so" in {
    given [A]: ProductHint[A] = ProductHint(ConfigFieldMapping(CamelCase, CamelCase))
    given ConfigReader[ConfWithCamelCase] = deriveReader

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

  it should "read pascal case config keys to pascal case fields when configured to do so" in {
    given [A]: ProductHint[A] = ProductHint(ConfigFieldMapping(CamelCase, PascalCase))
    given ConfigReader[ConfWithCamelCase] = deriveReader

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

  it should "allow customizing the field mapping only for specific types" in {
    given ProductHint[ConfWithCamelCase] = ProductHint(ConfigFieldMapping(CamelCase, CamelCase))
    given ConfigReader[ConfWithCamelCase] = deriveReader

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
    given ProductHint[Conf2] = ProductHint(allowUnknownKeys = false)

    case class Conf1(a: Int)
    given ConfigReader[Conf1] = deriveReader
    case class Conf2(a: Int)
    given ConfigReader[Conf2] = deriveReader

    val conf = ConfigFactory.parseString("""{
      conf {
        a = 1
        b = 2
      }
    }""")

    conf.getConfig("conf").to[Conf1] shouldBe Right(Conf1(1))
    conf.getConfig("conf").to[Conf2] should failWith(UnknownKey("b"), "b", stringConfigOrigin(4)) // borked
  }

  it should "accumulate all failures if the product hint doesn't allow unknown keys" in {
    given ProductHint[Conf] = ProductHint(allowUnknownKeys = false)
    case class Conf(a: Int)
    given ConfigReader[Conf] = deriveReader

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
    given ConfigReader[InnerConf] = deriveReader
    case class Conf(
        a: Int,
        b: String = "default",
        c: Int = 42,
        d: InnerConf = InnerConf(43, 44),
        e: Option[Int] = Some(45)
    )
    given ConfigReader[Conf] = deriveReader

    given ProductHint[Conf] = ProductHint(useDefaultArgs = false)

    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava)
    conf1.to[Conf].left.value.toList should contain theSameElementsAs Seq(
      ConvertFailure(KeyNotFound("b"), emptyConfigOrigin, ""),
      ConvertFailure(KeyNotFound("c"), emptyConfigOrigin, ""),
      ConvertFailure(KeyNotFound("d"), emptyConfigOrigin, "")
    )
  }

  it should "include candidate keys in failure reasons in case of a suspected misconfigured ProductHint" in {
    case class CamelCaseConf(camelCaseInt: Int, camelCaseString: String)
    given ConfigReader[CamelCaseConf] = deriveReader
    case class KebabCaseConf(kebabCaseInt: Int, kebabCaseString: String)
    given ConfigReader[KebabCaseConf] = deriveReader
    case class SnakeCaseConf(snakeCaseInt: Int, snakeCaseString: String)
    given ConfigReader[SnakeCaseConf] = deriveReader
    case class EnclosingConf(camelCaseConf: CamelCaseConf, kebabCaseConf: KebabCaseConf, snakeCaseConf: SnakeCaseConf)
    given ConfigReader[EnclosingConf] = deriveReader

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
