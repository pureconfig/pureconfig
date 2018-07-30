package pureconfig.module

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

package object http4s {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromString(str =>
      Uri.fromString(str).fold(
        err => Left(CannotConvert(str, "Uri", err.sanitized)),
        uri => Right(uri)))
}
