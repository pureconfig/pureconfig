package pureconfig.module

import org.http4s.Uri

import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigWriter}

package object http4s {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromString(str =>
      Uri.fromString(str).fold(err => Left(CannotConvert(str, "Uri", err.sanitized)), uri => Right(uri))
    )

  implicit val uriWriter: ConfigWriter[Uri] = ConfigWriter[String].contramap(_.renderString)
}
