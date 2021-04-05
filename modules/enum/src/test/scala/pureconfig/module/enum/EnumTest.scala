package pureconfig.module.enum

import _root_.enum.Enum
import com.typesafe.config.ConfigFactory
import org.scalatest.Inspectors

import pureconfig.BaseSuite
import pureconfig.error.CannotConvert
import pureconfig.generic.auto._
import pureconfig.syntax._

sealed trait Greeting

object Greeting {
  case object Hello extends Greeting
  case object GoodBye extends Greeting
  case object ShoutGoodBye extends Greeting

  final implicit val EnumInstance: Enum[Greeting] = Enum.derived[Greeting]
}

class EnumTest extends BaseSuite {

  "enum config convert" should "parse an enum" in Inspectors.forAll(Greeting.EnumInstance.values) { greeting =>
    val conf = ConfigFactory.parseString(s"""{greeting:"$greeting"}""")
    case class Conf(greeting: Greeting)
    conf.to[Conf].value shouldEqual Conf(greeting)
  }

  it should "politely refuse an invalid member" in {
    val conf = ConfigFactory.parseString(s"""{greeting:"Psych"}""")
    case class Conf(greeting: Greeting)
    conf.to[Conf] should failWithReason[CannotConvert]
  }
}
