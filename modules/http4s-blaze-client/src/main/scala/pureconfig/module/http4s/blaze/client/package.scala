package pureconfig.module.http4s.blaze

import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.error.CannotConvert
import pureconfig.{ConfigReader, ConfigWriter}

package object client {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromString(
      str =>
        Uri
          .fromString(str)
          .fold(
            err => Left(CannotConvert(str, "Uri", err.sanitized)),
            uri => Right(uri)
        )
    )

  implicit val uriWriter: ConfigWriter[Uri] =
    ConfigWriter[String].contramap(_.renderString)

  implicit val blazeClientBuilderReader: ConfigReader[BlazeClientBuilder] = {}

}
