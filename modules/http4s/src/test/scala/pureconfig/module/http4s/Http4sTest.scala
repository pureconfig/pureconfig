package pureconfig.module.http4s

import com.typesafe.config.ConfigFactory
import org.http4s.Uri

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigReader, ConfigSource, ConfigWriter}

class Http4sTest extends BaseSuite {

  "reading the uri config" should "parse the uri" in {
    val source = ConfigSource.string(s"""{uri:"http://http4s.org/"}""")

    source.at("uri").load[Uri].value shouldEqual Uri.unsafeFromString("http://http4s.org/")
  }

  "reading the uri config" should "get a CannotConvert error" in {
    val source = ConfigSource.string(s"""{uri:"\\\\"}""")

    val errors =
      ConfigReaderFailures(ConvertFailure(CannotConvert("\\", "Uri", "Invalid URI"), stringConfigOrigin(1), "uri"))

    source.at("uri").load[Uri].left.value shouldEqual errors
  }

  "Uri ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Uri" in {
    val expectedComplexUri =
      Uri.unsafeFromString(
        "http://www.google.ps/search?hl=en&client=firefox-a&hs=42F&rls=org.mozilla%3Aen-US%3Aofficial&q=The+type+%27Microsoft.Practices.ObjectBuilder.Locator%27+is+defined+in+an+assembly+that+is+not+referenced.+You+must+add+a+reference+to+assembly+&aq=f&aqi=&aql=&oq="
      )

    val configValue = ConfigWriter[Uri].to(expectedComplexUri)
    val Right(actualUri) = ConfigReader[Uri].from(configValue)

    actualUri shouldEqual expectedComplexUri
  }
}
