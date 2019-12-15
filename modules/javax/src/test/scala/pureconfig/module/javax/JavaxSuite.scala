package pureconfig.module.javax

import _root_.javax.security.auth.kerberos.KerberosPrincipal
import com.typesafe.config.ConfigFactory
import javax.security.auth.x500.X500Principal
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.generic.auto._
import pureconfig.syntax._

class JavaxSuite extends AnyFlatSpec with Matchers with EitherValues {

  case class K5Conf(principal: KerberosPrincipal)

  it should "be able to read a config with a KerberosPrincipal" in {
    val expected = "sample/principal@pureconfig"
    val config = ConfigFactory.parseString(s"""{ principal: "$expected" }""")
    config.to[K5Conf].right.value shouldEqual K5Conf(new KerberosPrincipal(expected))
  }

  case class X500Conf(principal: X500Principal)

  it should "be able to read a config with an X500Principal" in {
    val expected = "CN=Steve Kille,O=Isode Limited,C=GBg"
    val config = ConfigFactory.parseString(s"""{ principal: "$expected" }""")
    config.to[X500Conf].right.value shouldEqual X500Conf(new X500Principal(expected))
  }
}
