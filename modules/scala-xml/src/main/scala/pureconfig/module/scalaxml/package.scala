package pureconfig.module

import scala.xml.{ Elem, XML }

import pureconfig.ConvertHelpers.catchReadError
import pureconfig.{ ConfigReader, ConfigWriter }

/**
 * [[ConfigReader]] and [[ConfigWriter]] instances for Scala-XML's data structures.
 */
package object scalaxml {

  implicit def elemReader: ConfigReader[Elem] =
    ConfigReader.fromString[Elem](catchReadError(XML.loadString))

  implicit def elemWriter: ConfigWriter[Elem] =
    ConfigWriter.toDefaultString[Elem]

}
