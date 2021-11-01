package pureconfig.module

import com.comcast.ip4s.{Hostname, Port}

import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigWriter}

package object ip4s {

  implicit val hostnameWriter: ConfigWriter[Hostname] =
    ConfigWriter[String].contramap(_.toString)

  implicit val hostnameReader: ConfigReader[Hostname] =
    ConfigReader.fromString(str =>
      Hostname.fromString(str) match {
        case Some(hostname) => Right(hostname)
        case None => Left(CannotConvert(str, "Hostname", "Invalid hostname"))
      }
    )

  implicit val portWriter: ConfigWriter[Port] =
    ConfigWriter[Int].contramap(_.value)

  implicit val portReader: ConfigReader[Port] =
    ConfigReader[Int].emap(str =>
      Port.fromInt(str) match {
        case Some(port) => Right(port)
        case None => Left(CannotConvert(str.toString, "Port", "Invalid port"))
      }
    )
}
