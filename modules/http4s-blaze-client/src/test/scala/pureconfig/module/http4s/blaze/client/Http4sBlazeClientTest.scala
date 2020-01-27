package pureconfig.module.http4s.blaze.client

import java.util.concurrent.TimeUnit

import cats.effect.{ ContextShift, IO }
import com.typesafe.config.ConfigFactory
import pureconfig.BaseSuite
import pureconfig.syntax._

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration

class Http4sBlazeClientTest extends BaseSuite {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  "reading a BlazeClientBuilderConfig" should "allow creating a BlazeClientBuilder" in {
    val conf =
      ConfigFactory.parseString(s"""{ responseHeaderTimeout: "60 s" }""")

    val res = conf.to[BlazeClientBuilderConfig]

    res.right.value.configure[IO](global).responseHeaderTimeout should === {
      Duration.create(60, TimeUnit.SECONDS)
    }
  }

}
