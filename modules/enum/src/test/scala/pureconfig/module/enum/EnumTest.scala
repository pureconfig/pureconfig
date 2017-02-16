package pureconfig.module.enum

import com.typesafe.config.ConfigFactory
import org.scalatest.{ EitherValues, FlatSpec, Matchers }
import pureconfig.syntax._
import org.scalatest.Inspectors._
import _root_.enum.Enum
import pureconfig.error.CannotConvert

sealed trait Greeting

object Greeting {
  case object Hello extends Greeting
  case object GoodBye extends Greeting
  case object ShoutGoodBye extends Greeting

  final implicit val EnumInstance: Enum[Greeting] = Enum.derived[Greeting]
}

class EnumTest extends FlatSpec with Matchers with EitherValues {
  "enum config convert" should "parse an enum" in forAll(Greeting.EnumInstance.values) { greeting =>
    val conf = ConfigFactory.parseString(s"""{greeting:"$greeting"}""")
    case class Conf(greeting: Greeting)
    conf.to[Conf].right.value shouldEqual Conf(greeting)
  }

  it should "politely refuse an invalid member" in {
    val conf = ConfigFactory.parseString(s"""{greeting:"Psych"}""")
    case class Conf(greeting: Greeting)
    val failures = conf.to[Conf].left.value.toList
    failures should have size 1
    failures.head shouldBe a[CannotConvert]
  }

}

