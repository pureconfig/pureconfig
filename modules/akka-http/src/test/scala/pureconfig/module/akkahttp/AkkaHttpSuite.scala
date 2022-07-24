package pureconfig.module.akkahttp

import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigWriter}

class AkkaHttpSuite extends BaseSuite {

  val uri = Uri("https://doc.akka.io/docs/akka-http/current/index.html")
  val serverConf = "https://doc.akka.io/docs/akka-http/current/index.html"
  val config = configString(serverConf)

  val pathString = "/docs/akka-http/current/index.html"
  val path = Uri.Path("/docs/akka-http/current/index.html")

  behavior of "AkkaHttp module"

  it should "read the uri properly" in {
    config.to[Uri].value shouldEqual uri
  }

  it should "throw proper CannotConvert error when the uri is invalid" in {
    val config = configString("https://doc.akka.io/docs/akka-http/current folder with spaces/index.html")
    val errors = ConfigReaderFailures(
      ConvertFailure(
        CannotConvert(
          "https://doc.akka.io/docs/akka-http/current folder with spaces/index.html",
          "Uri",
          "Illegal URI reference: Invalid input ' ', expected '/', 'EOI', '#', '?' or pchar (line 1, column 43)"
        ),
        stringConfigOrigin(1),
        ""
      )
    )
    config.to[Uri].left.value shouldEqual errors
  }

  it should "be able to write the Uri as config" in {
    ConfigWriter[Uri].to(uri).unwrapped shouldEqual uri.toString
  }

  it should "read the path properly" in {
    configString(pathString).to[Uri.Path].value shouldEqual path
  }

  it should "be able to write the Uri.Path as a config" in {
    ConfigWriter[Uri.Path].to(path).unwrapped shouldEqual path.toString()
  }

  it should " throw proper CannotConvert error when the path is invalid" in {
    val config = configString("/docs/akka-http/%/index.html")
    val errors = ConfigReaderFailures(
      ConvertFailure(
        CannotConvert(
          "/docs/akka-http/%/index.html",
          "Uri.Path",
          "Illegal percent-encoding at pos 0"
        ),
        stringConfigOrigin(1),
        ""
      )
    )
    config.to[Uri.Path].left.value shouldEqual errors
  }
}
