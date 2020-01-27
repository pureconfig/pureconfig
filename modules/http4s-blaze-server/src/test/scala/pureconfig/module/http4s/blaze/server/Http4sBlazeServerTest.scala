package pureconfig.module.http4s.blaze.server

import cats.effect.{ ContextShift, IO, Timer }
import com.typesafe.config.ConfigFactory
import pureconfig.BaseSuite
import pureconfig.syntax._

import scala.concurrent.ExecutionContext.global

class Http4sBlazeServerTest extends BaseSuite {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  "reading the BlazeServerBuilder config" should "create the it" in {
    val conf = ConfigFactory.parseString(
      s"""{ host: "127.0.0.123", port: 1234, banner: ["a", "b"] }""")

    val res = conf.to[BlazeServerBuilderConfig]

    res.right.value
      .configure[IO]()
      .resource
      .use { server =>
        server.address.getAddress.getHostAddress should ===("127.0.0.123")
        server.address.getPort should ===(1234)
        IO.unit
      }
      .unsafeRunSync()
  }

}
