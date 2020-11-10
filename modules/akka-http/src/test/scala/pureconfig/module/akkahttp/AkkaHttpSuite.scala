package pureconfig.module.akkahttp

import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.model.Uri
import pureconfig.{BaseSuite, ConfigWriter}
import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.generic.auto._
import pureconfig.syntax._

class AkkaHttpSuite extends BaseSuite {

  case class ServerConfig(uri: Uri)

  val uri = Uri("https://doc.akka.io/docs/akka-http/current/index.html")
  val serverConf = s"""{uri:"https://doc.akka.io/docs/akka-http/current/index.html"}"""
  val config = ConfigFactory.parseString(serverConf)

  behavior of "AkkaHttp module"

  it should "read the uri properly" in {
    config.to[ServerConfig].value shouldEqual ServerConfig(uri)
  }

  it should " throw proper CannotConvert error" in {
    val conf =
      ConfigFactory.parseString(s"""{uri:"https://doc.akka.io/docs/akka-http/current folder with spaces/index.html"}""")
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
    conf.to[ServerConfig].left.value shouldEqual errors
  }

  it should "be able to write the Uri as config" in {
    ConfigWriter[ServerConfig].to(ServerConfig(uri)) shouldEqual config.root()
  }
}
