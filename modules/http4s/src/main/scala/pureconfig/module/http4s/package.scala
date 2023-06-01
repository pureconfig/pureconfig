package pureconfig.module

import scala.reflect.ClassTag

import org.http4s.{ParseResult, Uri}

import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigWriter}

package object http4s {
  private def mkConfigReader[A](f: String => ParseResult[A])(implicit ct: ClassTag[A]): ConfigReader[A] =
    ConfigReader.fromString { str =>
      val className = ct.runtimeClass.getSimpleName()
      f(str).fold(
        err => Left(CannotConvert(str, className, err.sanitized)),
        value => Right(value)
      )
    }

  implicit val uriReader: ConfigReader[Uri] =
    mkConfigReader[Uri](Uri.fromString)

  implicit val uriWriter: ConfigWriter[Uri] =
    ConfigWriter[String].contramap(_.renderString)

  implicit val uriSchemeReader: ConfigReader[Uri.Scheme] =
    mkConfigReader[Uri.Scheme](Uri.Scheme.fromString)

  implicit val uriSchemeWriter: ConfigWriter[Uri.Scheme] =
    ConfigWriter[String].contramap(_.value)

  implicit val uriPathReader: ConfigReader[Uri.Path] =
    ConfigReader.stringConfigReader.map(Uri.Path.unsafeFromString) // .fromString is deprecated

  implicit val uriPathWriter: ConfigWriter[Uri.Path] =
    ConfigWriter[String].contramap(_.renderString)

  implicit val uriHostReader: ConfigReader[Uri.Host] =
    mkConfigReader[Uri.Host](Uri.Host.fromString)

  implicit val uriHostWriter: ConfigWriter[Uri.Host] =
    ConfigWriter[String].contramap(_.renderString)

  implicit val uriIpv4AddressReader: ConfigReader[Uri.Ipv4Address] =
    mkConfigReader[Uri.Ipv4Address](Uri.Ipv4Address.fromString)

  implicit val uriIpv4AddressWriter: ConfigWriter[Uri.Ipv4Address] =
    ConfigWriter[String].contramap(_.renderString)

  implicit val uriIpv6AddressReader: ConfigReader[Uri.Ipv6Address] =
    mkConfigReader[Uri.Ipv6Address](Uri.Ipv6Address.fromString)

  implicit val uriIpv6AddressWriter: ConfigWriter[Uri.Ipv6Address] =
    ConfigWriter[String].contramap(_.value) // Uses .value so that we can round-trip as Ipv6Address
}
