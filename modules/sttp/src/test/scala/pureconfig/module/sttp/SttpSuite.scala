package pureconfig.module.sttp

import sttp.model.Uri
import sttp.model.Uri._
import com.typesafe.config.ConfigFactory
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
    val config = ConfigFactory.parseString("""{uri = "sttp.readthedocs.io"}""")

    val failure =
      ConvertFailure(
        reason = CannotConvert(value = "sttp.readthedocs.io", toType = "sttp.model.Uri", because = "empty.last"),
        origin = stringConfigOrigin(1),
        path = "uri"
      )

    config.to[AppConfig].left.value shouldBe ConfigReaderFailures(failure)
  }

}
