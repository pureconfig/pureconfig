package pureconfig.module.magnolia.auto

import scala.language.experimental.macros

import magnolia._
import pureconfig.{ConfigReader, Exported}
import pureconfig.generic.{CoproductHint, ProductHint}
import pureconfig.module.magnolia.{ExportedMagnolia, MagnoliaConfigReader}

/** An object that, when imported, provides implicit `ConfigReader` instances for value classes, tuples, case classes and
  * sealed traits. The generation of `ConfigReader`s is done by Magnolia.
  */
object reader {
  type Typeclass[A] = ConfigReader[A]

  def combine[A: ProductHint](ctx: CaseClass[ConfigReader, A]): ConfigReader[A] =
    MagnoliaConfigReader.combine(ctx)

  def dispatch[A: CoproductHint](ctx: SealedTrait[ConfigReader, A]): ConfigReader[A] =
    MagnoliaConfigReader.dispatch(ctx)

  implicit def exportReader[A]: Exported[ConfigReader[A]] = macro ExportedMagnolia.exportedMagnolia[ConfigReader, A]
}
