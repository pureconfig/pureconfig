package pureconfig
package generic

import scala.compiletime.testing._
import scala.jdk.CollectionConverters.given

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValueType}

import pureconfig.error._
import pureconfig.generic.ProductHint
import pureconfig.generic.semiauto._
import pureconfig.syntax._

class SemiautoDerivationSuite extends BaseSuite {

  behavior of "Semiauto Derivation"

  case class ConfWithCamelCaseInner(thisIsAnInt: Int, thisIsAnotherInt: Int)
  case class ConfWithCamelCase(
      camelCaseInt: Int,
      camelCaseString: String,
      camelCaseConf: Option[ConfWithCamelCaseInner]
  )

  val confWithCamelCase = ConfWithCamelCase(1, "foobar", Some(ConfWithCamelCaseInner(2, 3)))

  /** return all the keys in a `ConfigObject` */
  def allKeys(configObject: ConfigObject): Set[String] =
    configObject.toConfig().entrySet().asScala.flatMap(_.getKey.split('.')).toSet

  it should "derive mostly the same reader instance if inner instance is in scope" in {
    given ConfigReader[ConfWithCamelCaseInner] = deriveReader
    given ConfigReader[ConfWithCamelCase] = deriveReader

    val conf = ConfigFactory.parseString("""{
      camel-case-int = 1
      camel-case-string = "bar"
      camel-case-conf {
        this-is-an-int = 3
        this-is-another-int = 10
      }
    }""")

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", Some(ConfWithCamelCaseInner(3, 10))))
  }

  it should "throw an error during reader derivation if inner instance is missing" in {
    val errors = typeCheckErrors("""given ConfigReader[ConfWithCamelCase] = deriveReader""").map(_.message)

    atLeast(1, errors) should (startWith("Cannot derive ConfigReader for") and include("ConfWithCamelCaseInner"))
  }

  it should "derive mostly the same writer instance if inner instance is in scope" in {
    given ConfigWriter[ConfWithCamelCaseInner] = deriveWriter
    given ConfigWriter[ConfWithCamelCase] = deriveWriter

    val conf = confWithCamelCase.toConfig.asInstanceOf[ConfigObject]
    allKeys(conf) should contain theSameElementsAs Seq(
      "camel-case-int",
      "camel-case-string",
      "camel-case-conf",
      "this-is-an-int",
      "this-is-another-int"
    )
  }

  it should "throw an error during writer derivation if inner instance is missing" in {
    val errors = typeCheckErrors("""given ConfigWriter[ConfWithCamelCase] = deriveWriter""").map(_.message)

    atLeast(1, errors) should (startWith("Cannot derive ConfigWriter for") and include("ConfWithCamelCaseInner"))
  }
}
