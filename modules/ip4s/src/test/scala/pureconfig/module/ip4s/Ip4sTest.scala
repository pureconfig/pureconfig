package pureconfig.module.ip4s

import com.comcast.ip4s.{Hostname, Port}

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigWriter}

class Ip4sTest extends BaseSuite {

  "reading the hostname config" should "parse the hostname" in {
    val hostname = "0.0.0.0"
    configString(hostname).to[Hostname].value shouldEqual Hostname.fromString(hostname).get
  }

  "reading the hostname config" should "get a CannotConvert error" in {
    configString("...").to[Hostname].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert("...", "Hostname", "Invalid hostname"), stringConfigOrigin(1), "")
    )
  }

  "Hostname ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Hostname" in {
    val expectedHostname = Hostname.fromString("0.0.0.0").get

    val configValue = ConfigWriter[Hostname].to(expectedHostname)
    configValue.to[Hostname].value shouldEqual expectedHostname
  }

  "reading the port config" should "parse the port" in {
    val port = "8080"
    configString(port).to[Port].value shouldEqual Port.fromString(port).get
  }

  "reading the port config" should "get a CannotConvert error" in {
    configString("-1").to[Port].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert("-1", "Port", "Invalid port"), stringConfigOrigin(1), "")
    )
  }

  "Port ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Port" in {
    val expectedPort = Port.fromInt(8080).get

    val configValue = ConfigWriter[Port].to(expectedPort)
    configValue.to[Port].value shouldEqual expectedPort
  }
}
