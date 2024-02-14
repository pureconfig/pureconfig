package pureconfig.module.pekkohttp

import com.typesafe.config.ConfigFactory
import org.apache.pekko.http.scaladsl.model.Uri

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigWriter}

class PekkoHttpSuite extends BaseSuite {

  val uri = Uri("https://pekko.apache.org/docs/pekko-http/current/index.html")
  val serverConf = "https://pekko.apache.org/docs/pekko-http/current/index.html"
  val config = configString(serverConf)

  val pathString = "/docs/pekko-http/current/index.html"
  val path = Uri.Path("/docs/pekko-http/current/index.html")

  behavior of "PekkoHttp module"

  it should "read the uri properly" in {
    config.to[Uri].value shouldEqual uri
  }

  it should "throw proper CannotConvert error when the uri is invalid" in {
    val config = configString("https://pekko.apache.org/docs/pekko-http/current folder with spaces/index.html")
    val errors = ConfigReaderFailures(
      ConvertFailure(
        CannotConvert(
          "https://pekko.apache.org/docs/pekko-http/current folder with spaces/index.html",
          "Uri",
          "Illegal URI reference: Invalid input ' ', expected pchar, '/', '?', '#' or 'EOI' (line 1, column 49)"
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
    val config = configString("/docs/pekko-http/%/index.html")
    val errors = ConfigReaderFailures(
      ConvertFailure(
        CannotConvert(
          "/docs/pekko-http/%/index.html",
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
