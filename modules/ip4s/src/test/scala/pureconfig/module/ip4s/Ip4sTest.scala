package pureconfig.module.ip4s

import scala.reflect.ClassTag

import com.comcast.ip4s.Arbitraries._
import com.comcast.ip4s._
import com.typesafe.config._
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic.source.Position
import org.scalatest.{Inside, LoneElement}

import pureconfig._
import pureconfig.error._
import pureconfig.syntax._

class Ip4sTest extends BaseSuite with Inside with LoneElement {
  // By default `minSuccessfull` set to 10 in `ScalaCheckDrivenPropertyChecks`.
  // It is way too small for tests to be robust and reliable. Set to 100, which is used by default in ScalaCheck.
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

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

  private implicit val hostArbitrary: Arbitrary[Host] = Arbitrary(hostGenerator)

  private def testsFor[A: ConfigWriter: ConfigReader: Arbitrary, B: Arbitrary](
      configType: ConfigValueType
  )(implicit ctag: ClassTag[A], pos: Position) = {
    val cname = ctag.runtimeClass.getSimpleName

    s"ConfigWriter[$cname]" should s"write $cname to correct config type" in {
      forAll { (a: A) =>
        a.toConfig.valueType shouldEqual configType
      }
    }
    s"ConfigReader[$cname]" should s"fail for incorrect $cname" in {
      // TODO: filter out 0x0 char
      forAll { (b: B) =>
        val res = ConfigValueFactory.fromAnyRef(b).to[A]
        whenever(res.isLeft) { // skip occasional successful conversions
          inside(res.left.value.toList.loneElement) {
            case ConvertFailure(CannotConvert(value, `cname`, cause), Some(configOrigin), "") =>
              value shouldEqual b.toString
              cause shouldEqual s"Invalid $cname"
              configOrigin shouldEqual ConfigOriginFactory.newSimple
          }
        }
      }
    }
    checkArbitrary[A]
  }

  testsFor[Host, String](ConfigValueType.STRING)

  testsFor[Hostname, String](ConfigValueType.STRING)

  testsFor[IpAddress, String](ConfigValueType.STRING)

  testsFor[Ipv4Address, String](ConfigValueType.STRING)

  testsFor[Ipv6Address, String](ConfigValueType.STRING)

  testsFor[IDN, String](ConfigValueType.STRING)

  testsFor[Port, Int](ConfigValueType.NUMBER)

  // TODO: perhaps the tests below do not make sense anymore
  //       because everything should be covered by the above Scalacheck-driven tests already.
  //       Consider removing these explicit-value tests.

  "reading the hostname config" should "parse the hostname" in {
    val hostname = "0.0.0.0"
    configString(hostname).to[Hostname].value shouldEqual Hostname.fromString(hostname).get
  }

  "reading the hostname config" should "get a CannotConvert error" in {
    configString("...").to[Hostname].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(CannotConvert("...", "Hostname", "Invalid Hostname"), stringConfigOrigin(1), "")
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
      ConvertFailure(CannotConvert("-1", "Port", "Invalid Port"), stringConfigOrigin(1), "")
    )
  }

  "Port ConfigReader and ConfigWriter" should "be able to round-trip reading/writing of Port" in {
    val expectedPort = Port.fromInt(8080).get

    val configValue = ConfigWriter[Port].to(expectedPort)
    configValue.to[Port].value shouldEqual expectedPort
  }
}
