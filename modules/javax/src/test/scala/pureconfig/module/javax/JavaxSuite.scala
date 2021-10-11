package pureconfig.module.javax

import javax.security.auth.x500.X500Principal

import _root_.javax.security.auth.kerberos.KerberosPrincipal
import com.typesafe.config.ConfigFactory
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import pureconfig.syntax._
import pureconfig.{ConfigSource, ConfigWriter}

class JavaxSuite extends AnyFlatSpec with Matchers with EitherValues {

  it should "be able to read a config with a KerberosPrincipal" in {
    val expected = "sample/principal@pureconfig"
    val source = ConfigSource.string(s"""{ principal: "$expected" }""")
    source.at("principal").load[KerberosPrincipal].value shouldEqual new KerberosPrincipal(expected)
  }

  it should "be able to read a config with an X500Principal" in {
    val expected = "CN=Steve Kille,O=Isode Limited,C=GBg"
    val source = ConfigSource.string(s"""{ principal: "$expected" }""")
    source.at("principal").load[X500Principal].value shouldEqual new X500Principal(expected)
  }
}
