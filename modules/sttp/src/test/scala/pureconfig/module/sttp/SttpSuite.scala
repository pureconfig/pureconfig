package pureconfig.module.sttp

import com.typesafe.config.ConfigFactory
import sttp.model.Uri
import sttp.model.Uri._

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigSource}

class SttpSuite extends BaseSuite {

  behavior of "sttp module"

  it should "read uri" in {
    val uriStr = "https://sttp.readthedocs.io"
    configString(uriStr).to[Uri].value shouldBe Uri.unsafeParse(uriStr)
  }

  it should "handle error when reading uri" in {
    val uriStr = "http://example.com:80:80"
    configString(uriStr).to[Uri].left.value.head should matchPattern {
      case ConvertFailure(CannotConvert(str, "sttp.model.Uri", _), _, "") if str == uriStr =>
    }
  }
}
