package pureconfig.module.sttp

import com.typesafe.config.ConfigFactory
import sttp.model.Uri
import sttp.model.Uri._

import pureconfig.BaseSuite
import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.generic.auto._
import pureconfig.syntax._

class SttpSuite extends BaseSuite {

  case class AppConfig(uri: Uri)

  behavior of "sttp module"

  it should "read uri" in {
    val config = ConfigFactory.parseString("""{uri = "https://sttp.readthedocs.io"}""")

    config.to[AppConfig].value shouldBe AppConfig(uri"https://sttp.readthedocs.io")
  }

  it should "handle error when reading uri" in {
    val config = ConfigFactory.parseString("""{uri = "http://example.com:80:80"}""")

    config.to[AppConfig].left.value.head should matchPattern {
      case ConvertFailure(CannotConvert("http://example.com:80:80", "sttp.model.Uri", _), _, "uri") =>
    }
  }
}
