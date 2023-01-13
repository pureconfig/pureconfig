package pureconfig.module.http4s022

import com.typesafe.config.ConfigFactory
import org.http4s.Uri

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigReader, ConfigWriter}

class Http4s022Test extends BaseSuite {

  "reading the uri config" should "parse the uri" in {
    val uriStr = "http://http4s.org/"
    configString(uriStr).to[Uri].value shouldEqual Uri.unsafeFromString(uriStr)
  }

  "reading the uri config" should "get a CannotConvert error" in {
    configString("\\\\").to[Uri].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert("\\", "Uri", "Invalid URI"), stringConfigOrigin(1), "")
    )
  }

  "Uri ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Uri" in {
    val expectedComplexUri =
      Uri.unsafeFromString(
        "http://www.google.ps/search?hl=en&client=firefox-a&hs=42F&rls=org.mozilla%3Aen-US%3Aofficial&q=The+type+%27Microsoft.Practices.ObjectBuilder.Locator%27+is+defined+in+an+assembly+that+is+not+referenced.+You+must+add+a+reference+to+assembly+&aq=f&aqi=&aql=&oq="
      )

    val configValue = ConfigWriter[Uri].to(expectedComplexUri)
    configValue.to[Uri].value shouldEqual expectedComplexUri
  }
}
