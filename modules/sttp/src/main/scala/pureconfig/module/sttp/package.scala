package pureconfig.module

import _root_.sttp.model._
import _root_.sttp.model.Uri._

import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

import scala.util.{Failure, Success, Try}

package object sttp {

  implicit val reader: ConfigReader[Uri] =
    ConfigReader.fromNonEmptyString { str =>
      Try(uri"$str") match {
        case Success(uri) => Right(uri)
        case Failure(ex) => Left(CannotConvert(str, "sttp.model.Uri", ex.getMessage))
      }
    }

}
