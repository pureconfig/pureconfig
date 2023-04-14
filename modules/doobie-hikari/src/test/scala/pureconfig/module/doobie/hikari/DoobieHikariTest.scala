package pureconfig.module.doobie.hikari

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.config.ConfigFactory
import doobie.hikari.Config
import org.scalactic.TypeCheckedTripleEquals

import pureconfig.BaseSuite
import pureconfig.syntax._

class DoobieHikariTest extends BaseSuite with TypeCheckedTripleEquals {

  "reading a doobie.hikari.Config" should "allow creating a HikariConfig" in {
    val conf = ConfigFactory.parseString(s"""{
      |    username = "postgres"
      |    password = "postgres"
      |    jdbc-url = "jdbc:postgresql://localhost:5432/postgres?targetServerType=primary&connectTimeout=10"
      |    driver-class-name = "org.postgresql.Driver"
      |}""".stripMargin)
    val Right(res) = conf.to[Config]
    res.username should ===(Some("postgres"))
    res.password should ===(Some("postgres"))
    res.jdbcUrl should ===(Some("jdbc:postgresql://localhost:5432/postgres?targetServerType=primary&connectTimeout=10"))
    res.driverClassName should ===(Some("org.postgresql.Driver"))
    val config = Config.makeHikariConfig[IO](res).unsafeRunSync()
    config.validate()
  }
}
