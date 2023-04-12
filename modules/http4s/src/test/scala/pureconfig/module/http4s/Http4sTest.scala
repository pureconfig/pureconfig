package pureconfig.module.http4s

import com.typesafe.config.ConfigFactory
import org.http4s.Uri

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigReader, ConfigWriter}

class Http4sTest extends BaseSuite {

  "reading the uri config" should "parse the uri" in {
    val uriStr = "http://http4s.org/"
    configString(uriStr).to[Uri].value shouldEqual Uri.unsafeFromString(uriStr)
  }

  "reading the uri config" should "get a CannotConvert error" in {
    configString("\\\\").to[Uri].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert("\\", "Uri", "Invalid URI"), stringConfigOrigin(1), "")
    )
  }

  "reading a uri scheme" should "parse the scheme" in {
    configString("https").to[Uri.Scheme].value shouldEqual Uri.Scheme.https
    configString("http").to[Uri.Scheme].value shouldEqual Uri.Scheme.http
    configString("mongodb").to[Uri.Scheme].value shouldEqual Uri.Scheme.unsafeFromString("mongodb")
  }

  "reading an invalid uri scheme" should "get a CannotConvert error" in {
    configString("\\\\").to[Uri.Scheme].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert("\\", "Scheme", "Invalid scheme"), stringConfigOrigin(1), "")
    )
  }

  "reading a uri path" should "parse the path" in {
    configString("/path").to[Uri.Path].value shouldEqual Uri.Path.unsafeFromString("/path")
    configString("/path/with/lots/of/segments").to[Uri.Path].value shouldEqual Uri.Path.unsafeFromString(
      "/path/with/lots/of/segments"
    )
    configString("relative/path").to[Uri.Path].value shouldEqual Uri.Path.unsafeFromString("relative/path")
  }

  "reading a uri host" should "parse the host" in {
    configString("localhost").to[Uri.Host].value shouldEqual Uri.Host.unsafeFromString("localhost")
    configString("192.168.1.1").to[Uri.Host].value shouldEqual Uri.Host.unsafeFromString("192.168.1.1")
    configString("www.foo.com").to[Uri.Host].value shouldEqual Uri.Host.unsafeFromString("www.foo.com")
    configString("[2001:db8:85a3:8d3:1319:8a2e:370:7344]").to[Uri.Host].value shouldEqual Uri.Host.unsafeFromString(
      "[2001:db8:85a3:8d3:1319:8a2e:370:7344]"
    )
  }

  "reading an invalid uri host" should "get a CannotConvert error" in {
    configString(":/?#[]@").to[Uri.Host].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert(":/?#[]@", "Host", "Invalid host"), stringConfigOrigin(1), "")
    )
  }

  "reading an ipv4 address" should "parse the address" in {
    configString("192.168.1.1").to[Uri.Ipv4Address].value shouldEqual Uri.Ipv4Address.unsafeFromString("192.168.1.1")
  }

  "reading an invalid ipv4 address" should "get a CannotConvert error" in {
    configString(":/?#[]@").to[Uri.Ipv4Address].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert(":/?#[]@", "Ipv4Address", "Invalid IPv4 Address"), stringConfigOrigin(1), "")
    )
  }

  "reading an ipv6 address" should "parse the address" in {
    configString("2001:db8:85a3:8d3:1319:8a2e:370:7344").to[Uri.Ipv6Address].value shouldEqual Uri.Ipv6Address
      .unsafeFromString(
        "2001:db8:85a3:8d3:1319:8a2e:370:7344"
      )
  }

  "reading an invalid ipv6 address" should "get a CannotConvert error" in {
    configString(":/?#[]@").to[Uri.Ipv6Address].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert(":/?#[]@", "Ipv6Address", "Invalid IPv6 address"), stringConfigOrigin(1), "")
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

  "Uri.Scheme ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Uri.Scheme" in {
    val expectedScheme = Uri.Scheme.unsafeFromString("mongodb")
    val configValue = ConfigWriter[Uri.Scheme].to(expectedScheme)
    configValue.to[Uri.Scheme].value shouldEqual expectedScheme
  }

  "Uri.Path ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Uri.Path" in {
    val expectedPath = Uri.Path.unsafeFromString("relative/path/to/resource.txt")
    val configValue = ConfigWriter[Uri.Path].to(expectedPath)
    configValue.to[Uri.Path].value shouldEqual expectedPath
  }

  "Uri.Host ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Uri.Host" in {
    val expectedHost = Uri.Host.unsafeFromString("[2001:db8:85a3:8d3:1319:8a2e:370:7344]")
    val configValue = ConfigWriter[Uri.Host].to(expectedHost)
    configValue.to[Uri.Host].value shouldEqual expectedHost
  }

  "Uri.Ipv4Address ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Uri.Ipv4Address" in {
    val expectedHost = Uri.Ipv4Address.unsafeFromString("192.168.1.1")
    val configValue = ConfigWriter[Uri.Ipv4Address].to(expectedHost)
    configValue.to[Uri.Ipv4Address].value shouldEqual expectedHost
  }

  "Uri.Ipv6Address ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Uri.Ipv6Address" in {
    val expectedHost = Uri.Ipv6Address.unsafeFromString("2001:db8:85a3:8d3:1319:8a2e:370:7344")
    val configValue = ConfigWriter[Uri.Ipv6Address].to(expectedHost)
    configValue.to[Uri.Ipv6Address].value shouldEqual expectedHost
  }
}
