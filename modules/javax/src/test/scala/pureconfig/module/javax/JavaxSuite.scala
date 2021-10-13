package pureconfig.module.javax

import javax.security.auth.x500.X500Principal

import _root_.javax.security.auth.kerberos.KerberosPrincipal
import com.typesafe.config.ConfigFactory

import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigWriter}

class JavaxSuite extends BaseSuite {

  it should "be able to read a config with a KerberosPrincipal" in {
    val expected = "sample/principal@pureconfig"
    configString(expected).to[KerberosPrincipal].value shouldEqual new KerberosPrincipal(expected)
  }

  it should "be able to read a config with an X500Principal" in {
    val expected = "CN=Steve Kille,O=Isode Limited,C=GBg"
    configString(expected).to[X500Principal].value shouldEqual new X500Principal(expected)
  }
}
