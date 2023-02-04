package pureconfig.module

import com.comcast.ip4s._

import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigWriter}

package object ip4s {

  private def mkConfigReader[From, To](f: From => Option[To])(
      cname: String,
      uname: String
  )(implicit From: ConfigReader[From]): ConfigReader[To] = From.emap { from =>
    f(from).toRight(CannotConvert(from.toString, cname, s"Invalid $uname"))
  }

  implicit val hostWriter: ConfigWriter[Host] =
    ConfigWriter[String].contramap(_.toString)

  implicit val hostReader: ConfigReader[Host] =
    mkConfigReader[String, Host](Host.fromString)("Host", "host")

  implicit val ipAddressWriter: ConfigWriter[IpAddress] =
    ConfigWriter[String].contramap(_.toString)

  implicit val ipAddressReader: ConfigReader[IpAddress] =
    mkConfigReader[String, IpAddress](IpAddress.fromString)("IpAddress", "IP address")

  implicit val ipv4AddressWriter: ConfigWriter[Ipv4Address] =
    ConfigWriter[String].contramap(_.toString)

  implicit val ipv4AddressReader: ConfigReader[Ipv4Address] =
    mkConfigReader[String, Ipv4Address](Ipv4Address.fromString)("Ipv4Address", "IPv4 address")

  implicit val ipv6AddressWriter: ConfigWriter[Ipv6Address] =
    ConfigWriter[String].contramap(_.toString)

  implicit val ipv6AddressReader: ConfigReader[Ipv6Address] =
    mkConfigReader[String, Ipv6Address](Ipv6Address.fromString)("Ipv6Address", "IPv6 address")

  implicit val hostnameWriter: ConfigWriter[Hostname] =
    ConfigWriter[String].contramap(_.toString)

  implicit val hostnameReader: ConfigReader[Hostname] =
    mkConfigReader[String, Hostname](Hostname.fromString)("Hostname", "hostname")

  implicit val idnWriter: ConfigWriter[IDN] =
    ConfigWriter[String].contramap(_.toString)

  implicit val idnReader: ConfigReader[IDN] =
    mkConfigReader[String, IDN](IDN.fromString)("IDN", "IDN")

  implicit val portWriter: ConfigWriter[Port] =
    ConfigWriter[Int].contramap(_.value)

  implicit val portReader: ConfigReader[Port] =
    mkConfigReader[Int, Port](Port.fromInt)("Port", "port")
}
