package pureconfig
package generic

import scala.compiletime.testing._
import scala.jdk.CollectionConverters.given

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValueType}

import pureconfig.error._
import pureconfig.generic.ProductHint
import pureconfig.generic.semiauto._
import pureconfig.syntax._

class DerivationFlowSuite extends BaseSuite {

  behavior of "DerivationFlow"

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

  it should "normal reader derivation should work the same" in {
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

  it should "reader derivation without embedded class should fail" in {
    //   given ConfigReader[ConfWithCamelCaseInner] = deriveReader
    val errors = typeCheckErrors("""given ConfigReader[ConfWithCamelCase] = deriveReader""")

    errors shouldBe List("Some Error")
  }

}
