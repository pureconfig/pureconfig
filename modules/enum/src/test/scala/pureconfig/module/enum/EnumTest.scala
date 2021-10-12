package pureconfig.module.enum

import _root_.enum.Enum
import org.scalatest.Inspectors

import pureconfig.BaseSuite
import pureconfig.error.CannotConvert
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
    configValue(s""""$greeting"""").to[Greeting].value shouldEqual greeting
  }

  it should "politely refuse an invalid member" in {
    configValue(s""""Psych"""").to[Greeting] should failWithReason[CannotConvert]
  }
}
