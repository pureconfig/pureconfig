package pureconfig.module

import akka.http.scaladsl.model.{IllegalUriException, Uri}
import akka.http.scaladsl.model.Uri.ParsingMode
import pureconfig.{ConfigReader, ConfigWriter}
import pureconfig.error.{CannotConvert, ExceptionThrown}

import scala.util.Try

package object akkahttp {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromString(str =>
      Try(Uri(str, ParsingMode.Strict)).toEither.left
        .map {
          case err: IllegalUriException => CannotConvert(str, "Uri", err.info.summary)
          case err => ExceptionThrown(err)
        }
    )

  implicit val uriWriter: ConfigWriter[Uri] = ConfigWriter[String].contramap(_.toString)
}
