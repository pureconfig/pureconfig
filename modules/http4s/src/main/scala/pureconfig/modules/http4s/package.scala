package pureconfig.modules

import org.http4s.Uri
import pureconfig.ConfigReader

package object http4s {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader[String].map(str => Uri(path = str))
}
