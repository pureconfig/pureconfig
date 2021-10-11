package pureconfig.module.enum

import _root_.enum.Enum
import com.typesafe.config.ConfigFactory
import org.scalatest.Inspectors

import pureconfig.error.CannotConvert
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigSource}

sealed trait Greeting

object Greeting {
  case object Hello extends Greeting
  case object GoodBye extends Greeting
  case object ShoutGoodBye extends Greeting

  final implicit val EnumInstance: Enum[Greeting] = Enum.derived[Greeting]
}

class EnumTest extends BaseSuite {

  "enum config convert" should "parse an enum" in Inspectors.forAll(Greeting.EnumInstance.values) { greeting =>
    val source = ConfigSource.string(s"""{greeting:"$greeting"}""")
    source.at("greeting").load[Greeting].value shouldEqual greeting
  }

  it should "politely refuse an invalid member" in {
    val source = ConfigSource.string(s"""{greeting:"Psych"}""")
    source.at("greeting").load[Greeting] should failWithReason[CannotConvert]
  }
}
