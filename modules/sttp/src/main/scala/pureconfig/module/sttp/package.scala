package pureconfig.module

import scala.util.{Failure, Success, Try}

import _root_.sttp.model.Uri._
import _root_.sttp.model._

import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

package object sttp {

  implicit val reader: ConfigReader[Uri] =
    ConfigReader.fromNonEmptyString { str =>
      Try(uri"$str") match {
        case Success(uri) => Right(uri)
        case Failure(ex) => Left(CannotConvert(str, "sttp.model.Uri", ex.getMessage))
      }
    }

}
