package pureconfig.module.ip4s

import scala.reflect.ClassTag

import com.comcast.ip4s.Arbitraries._
import com.comcast.ip4s._
import com.typesafe.config._
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic.source.Position
import org.scalatest.Inside

import pureconfig._
import pureconfig.error._
import pureconfig.syntax._

class Ip4sTest extends BaseSuite with Inside {
  // By default `minSuccessfull` set to 10 in `ScalaCheckDrivenPropertyChecks`.
  // It is way too small for tests to be robust and reliable. Set to 100, which is used by default in ScalaCheck.
  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  // This generator is missing from `com.comcast.ip4s.Arbitraries` for some reason.
  private val hostGenerator: Gen[Host] =
    Gen.oneOf(
      ipGenerator,
      hostnameGenerator,
      // `idnGenerator` sometimes produces values that can be parsed as a valid `Hostname`.
      // However, `Hostname` has a higher priority over `IDN` in `Host.fromString`.
      // Therefore filter such values out.
      idnGenerator.filter(idn => Hostname.fromString(idn.toString).isEmpty)
    )

  private def testsFor[A: ConfigWriter: ConfigReader, B: Arbitrary](
      uname: String,
      configType: ConfigValueType,
      gen: Gen[A],
      enc: A => B,
      dec: B => Option[A]
  )(implicit ctag: ClassTag[A], pos: Position) = {
    val cname = ctag.runtimeClass.getSimpleName

    s"ConfigWriter[$cname]" should s"write $uname to config" in {
      forAll(gen) { a =>
        val b = enc(a)
        val config = a.toConfig
        config.valueType shouldEqual configType
        config.unwrapped() shouldEqual b
      }
    }
    s"ConfigReader[$cname]" should s"read $uname from config" in {
      forAll(gen) { a =>
        val config = ConfigValueFactory.fromAnyRef(enc(a))
        config.valueType shouldEqual configType
        config.to[A].value shouldEqual a
      }
    }
    it should s"fail for incorrect $uname" in {
      // TODO: filter out 0x0 char
      forAll { (b: B) =>
        whenever(dec(b).isEmpty) {
          inside(ConfigValueFactory.fromAnyRef(b).to[A].left.value) {
            case ConfigReaderFailures(
                  ConvertFailure(
                    CannotConvert(value, `cname`, cause),
                    Some(configOrigin),
                    ""
                  )
                ) =>
              value shouldEqual b.toString
              cause shouldEqual s"Invalid $uname"
              configOrigin shouldEqual ConfigOriginFactory.newSimple
          }
        }
      }
    }
  }

  testsFor[Host, String](
    "host",
    ConfigValueType.STRING,
    hostGenerator,
    _.toString,
    Host.fromString
  )

  testsFor[Hostname, String](
    "hostname",
    ConfigValueType.STRING,
    hostnameGenerator,
    _.toString,
    Hostname.fromString
  )

  testsFor[IpAddress, String](
    "IP address",
    ConfigValueType.STRING,
    ipGenerator,
    _.toString,
    IpAddress.fromString
  )

  testsFor[Ipv4Address, String](
    "IPv4 address",
    ConfigValueType.STRING,
    ipv4Generator,
    _.toString,
    Ipv4Address.fromString
  )

  testsFor[Ipv6Address, String](
    "IPv6 address",
    ConfigValueType.STRING,
    ipv6Generator,
    _.toString,
    Ipv6Address.fromString
  )

  testsFor[IDN, String](
    "IDN",
    ConfigValueType.STRING,
    idnGenerator,
    _.toString,
    IDN.fromString
  )

  testsFor[Port, Int](
    "port",
    ConfigValueType.NUMBER,
    portGenerator,
    _.value,
    Port.fromInt
  )

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
