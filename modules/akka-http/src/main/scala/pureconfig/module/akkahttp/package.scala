package pureconfig.module

import scala.util.Try

import akka.http.scaladsl.model.Uri.ParsingMode
import akka.http.scaladsl.model.{IllegalUriException, Uri}

import pureconfig.error.{CannotConvert, ExceptionThrown}
import pureconfig.{ConfigReader, ConfigWriter}

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

  implicit val pathReader: ConfigReader[Uri.Path] = ConfigReader.fromString(s =>
    Try(Uri.Path(s)).toEither.left.map {
      case err: IllegalUriException => CannotConvert(s, "Uri.Path", err.info.summary)
      case err => ExceptionThrown(err)
    }
  )

  implicit val pathWriter: ConfigWriter[Uri.Path] = ConfigWriter[String].contramap(_.toString)
}
