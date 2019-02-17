package pureconfig.module

import com.softwaremill.sttp._
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

import scala.util.Try

package object sttp {

  implicit val reader: ConfigReader[Uri] =
    ConfigReader.fromNonEmptyString { str ⇒
      Try(uri"$str").fold(
        ex ⇒ Left(CannotConvert(str, "com.softwaremill.sttp.Uri", ex.getMessage)),
        uri ⇒ Right(uri))
    }

}
