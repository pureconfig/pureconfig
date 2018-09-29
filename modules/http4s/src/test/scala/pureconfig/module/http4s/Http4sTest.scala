package pureconfig.module.http4s

import com.typesafe.config.ConfigFactory
import org.http4s.Uri
import pureconfig.BaseSuite
import pureconfig.error.{ CannotConvert, ConfigReaderFailures, ConvertFailure }
import pureconfig.generic.auto._
import pureconfig.syntax._

class Http4sTest extends BaseSuite {

  case class ServerConfig(uri: Uri)

  "reading the uri config" should "parse the uri" in {
    val conf = ConfigFactory.parseString(s"""{uri:"http://http4s.org/"}""")

    conf.to[ServerConfig].right.value shouldEqual ServerConfig(Uri.unsafeFromString("http://http4s.org/"))
  }

  "reading the uri config" should "get a CannotConvert error" in {
    val conf = ConfigFactory.parseString(s"""{uri:"\\\\"}""")

    val errors = ConfigReaderFailures(ConvertFailure(CannotConvert("\\", "Uri", "Invalid URI"), None, "uri"))

    conf.to[ServerConfig].left.value shouldEqual errors
  }
}
