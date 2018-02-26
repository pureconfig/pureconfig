package pureconfig.modules.http4s

import pureconfig.syntax._
import com.typesafe.config.ConfigFactory
import org.http4s.Uri
import pureconfig.BaseSuite

class Http4sTest extends BaseSuite {

  case class ServerConfig(uri: Uri)

  "reading the uri config" should "parse the uri" in {
    val conf = ConfigFactory.parseString(s"""{uri:"http://http4s.org/"}""")

    conf.to[ServerConfig].right.value shouldEqual ServerConfig(Uri.unsafeFromString("http://http4s.org/"))
  }
}
