package pureconfig.generic

import scala.language.experimental.macros

import pureconfig._

object auto {
  implicit def exportReader[A]: Exported[ConfigReader[A]] = macro ExportMacros.exportReader[A]
  implicit def exportWriter[A]: Exported[ConfigWriter[A]] = macro ExportMacros.exportWriter[A]
}
