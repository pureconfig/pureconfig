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
    val source = ConfigSource.string("""{uri = "https://sttp.readthedocs.io"}""")

    source.at("uri").load[Uri].value shouldBe uri"https://sttp.readthedocs.io"
  }

  it should "handle error when reading uri" in {
    val source = ConfigSource.string("""{uri = "http://example.com:80:80"}""")

    source.at("uri").load[Uri].left.value.head should matchPattern {
      case ConvertFailure(CannotConvert("http://example.com:80:80", "sttp.model.Uri", _), _, "uri") =>
    }
  }
}
