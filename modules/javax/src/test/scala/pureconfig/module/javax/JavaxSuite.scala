package pureconfig.module.javax

import _root_.javax.security.auth.kerberos.KerberosPrincipal

import com.typesafe.config.{ ConfigFactory }
import org.scalatest._
import pureconfig.syntax._

class JavaxSuite extends FlatSpec with Matchers with EitherValues {

  case class Conf(principal: KerberosPrincipal)

  it should "be able to read a config with a KerberosPrincipal" in {
    val expected = "sample/principal@pureconfig"
    val config = ConfigFactory.parseString(s"""{ principal: "$expected" }""")
    config.to[Conf].right.value shouldEqual Conf(new KerberosPrincipal(expected))
  }
}
