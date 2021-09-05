package pureconfig.module.zioconfig

import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.scalacheck.Arbitrary
import zio.config._
import zio.config.magnolia.DeriveConfigDescriptor.descriptor

import pureconfig.BaseSuite
import pureconfig.error.WrongType

class ZioConfigSuite extends BaseSuite {
  case class MyConfig(ldap: String, port: Int, dburl: String)

  implicit val desc: ConfigDescriptor[MyConfig] = descriptor
  implicit val arb: Arbitrary[MyConfig] = Arbitrary {
    Arbitrary.arbitrary[(String, Int, String)].map((MyConfig.apply _).tupled)
  }

  checkArbitrary[MyConfig]

  checkFailure[MyConfig, WrongType](ConfigValueFactory.fromAnyRef("string"))
  checkFailure[MyConfig, ZioConfigReadError[Nothing]](ConfigFactory.empty.root)
}
