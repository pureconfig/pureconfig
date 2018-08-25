package pureconfig.generic

import scala.language.experimental.macros

import pureconfig._

/**
 * An object that, when imported, provides implicit `ConfigReader` and `ConfigWriter` instances for value classes,
 * tuples, case classes and sealed traits.
 */
object auto {
  implicit def exportReader[A]: Exported[ConfigReader[A]] = macro ExportMacros.exportDerivedReader[A]
  implicit def exportWriter[A]: Exported[ConfigWriter[A]] = macro ExportMacros.exportDerivedWriter[A]
}
