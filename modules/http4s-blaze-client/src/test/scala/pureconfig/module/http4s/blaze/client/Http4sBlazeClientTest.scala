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
      ConfigFactory.parseString(s"""
           |{ 
           |  responseHeaderTimeout: "1 s"
           |  idleTimeout: "2 s"
           |  requestTimeout: "3 s"
           |  maxTotalConnections: 4
           |  maxWaitQueueLimit: 5
           |  checkEndpointAuthentication: true
           |  maxResponseLineSize: 7
           |  maxHeaderLength: 8
           |  maxChunkSize: 9
           |  chunkBufferMaxSize: 10
           |}
           |""".stripMargin)

    val res = conf.to[BlazeClientBuilderConfig]
    val clientBuilder = res.right.value.configure[IO](global)

    clientBuilder.responseHeaderTimeout should === {
      Duration.create(1, TimeUnit.SECONDS)
    }
    clientBuilder.idleTimeout should === {
      Duration.create(2, TimeUnit.SECONDS)
    }
    clientBuilder.requestTimeout should === {
      Duration.create(3, TimeUnit.SECONDS)
    }
    clientBuilder.maxTotalConnections should === {
      4
    }
    clientBuilder.maxWaitQueueLimit should === {
      5
    }
    res.right.value
      .configure[IO](global)
      .checkEndpointIdentification should === {
        true
      }
    clientBuilder.maxResponseLineSize should === {
      7
    }
    clientBuilder.maxHeaderLength should === {
      8
    }
    clientBuilder.maxChunkSize should === {
      9
    }
    clientBuilder.chunkBufferMaxSize should === {
      10
    }

    clientBuilder.resource
      .use { _ =>
        IO.unit
      }
      .unsafeRunSync()
  }

}
