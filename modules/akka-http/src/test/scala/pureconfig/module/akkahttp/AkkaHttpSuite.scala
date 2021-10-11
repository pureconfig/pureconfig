package pureconfig.module.akkahttp

import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigSource, ConfigWriter}

class AkkaHttpSuite extends BaseSuite {

  val uri = Uri("https://doc.akka.io/docs/akka-http/current/index.html")
  val serverConf = s"""{uri:"https://doc.akka.io/docs/akka-http/current/index.html"}"""
  val source = ConfigSource.string(serverConf)

  behavior of "AkkaHttp module"

  it should "read the uri properly" in {
    source.at("uri").load[Uri].value shouldEqual uri
  }

  it should " throw proper CannotConvert error" in {
    val source =
      ConfigSource.string(s"""{uri:"https://doc.akka.io/docs/akka-http/current folder with spaces/index.html"}""")
    val errors = ConfigReaderFailures(
      ConvertFailure(
        CannotConvert(
          "https://doc.akka.io/docs/akka-http/current folder with spaces/index.html",
          "Uri",
          "Illegal URI reference: Invalid input ' ', expected '/', 'EOI', '#', '?' or pchar (line 1, column 43)"
        ),
        stringConfigOrigin(1),
        "uri"
      )
    )
    source.at("uri").load[Uri].left.value shouldEqual errors
  }

  it should "be able to write the Uri as config" in {
    ConfigWriter[Uri].to(uri).unwrapped shouldEqual uri.toString
  }
}
