package pureconfig.module

import akka.http.scaladsl.model.{ IllegalUriException, Uri }
import akka.http.scaladsl.model.Uri.ParsingMode
import pureconfig.{ ConfigReader, ConfigWriter }
import pureconfig.error.CannotConvert

import scala.util.Try

package object akkahttp {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromString(str =>
      Try(Uri(str, ParsingMode.Strict)).fold(
        {
          case err: IllegalUriException => Left(CannotConvert(str, "Uri", err.info.summary))
          case err => Left(CannotConvert(str, "Uri", err.getMessage.replace('^', " ".charAt(0)).trim))
        },
        uri => Right(uri)))

  implicit val uriWriter: ConfigWriter[Uri] = ConfigWriter[String].contramap(_.toString)
}
