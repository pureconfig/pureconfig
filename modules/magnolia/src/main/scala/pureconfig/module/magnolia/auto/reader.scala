package pureconfig.module.magnolia.auto

import scala.language.experimental.macros

import magnolia1._

import pureconfig.generic.{CoproductHint, ProductHint}
import pureconfig.module.magnolia.{ExportedMagnolia, MagnoliaConfigReader}
import pureconfig.{ConfigReader, Exported}

/** An object that, when imported, provides implicit `ConfigReader` instances for value classes, tuples, case classes
  * and sealed traits. The generation of `ConfigReader`s is done by Magnolia.
  */
object reader {
  type Typeclass[A] = ConfigReader[A]

  def join[A: ProductHint](ctx: CaseClass[ConfigReader, A]): ConfigReader[A] =
    MagnoliaConfigReader.join(ctx)

  def split[A: CoproductHint](ctx: SealedTrait[ConfigReader, A]): ConfigReader[A] =
    MagnoliaConfigReader.split(ctx)

  implicit def exportReader[A]: Exported[ConfigReader[A]] = macro ExportedMagnolia.exportedMagnolia[ConfigReader, A]
}
