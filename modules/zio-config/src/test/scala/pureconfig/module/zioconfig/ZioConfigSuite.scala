package pureconfig.module.zioconfig

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import zio.Config._
import zio.config._
import zio.{Config, IO}

import pureconfig.BaseSuite
import pureconfig.error.WrongType

class ZioConfigSuite extends BaseSuite {
  case class MyConfig(ldap: String, port: Int, dburl: String)

  implicit val desc: Config[MyConfig] =
    (string("LDAP") zip int("PORT") zip string("DB_URL")).to[MyConfig]

  val conf1 = ConfigFactory
    .parseString(
      """
      |LDAP = "ldap://localhost"
      |PORT = 1234
      |DB_URL = "jdbc:mysql://localhost:3306/test"
      |""".stripMargin
    )
    .root()

  val res1 = MyConfig("ldap://localhost", 1234, "jdbc:mysql://localhost:3306/test")

  checkRead[MyConfig](conf1 -> res1)

  checkFailure[MyConfig, WrongType](ConfigValueFactory.fromAnyRef("string"))
  checkFailure[MyConfig, ZioConfigReadError[Nothing]](ConfigFactory.empty.root)
}
