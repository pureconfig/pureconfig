package pureconfig.module.magnolia.auto

import scala.language.experimental.macros
import scala.reflect.ClassTag

import magnolia1._

import pureconfig.generic.{CoproductHint, ProductHint}
import pureconfig.module.magnolia.{ExportedMagnolia, MagnoliaConfigWriter}
import pureconfig.{ConfigWriter, Exported}

/** An object that, when imported, provides implicit `ConfigWriter` instances for value classes, tuples, case classes
  * and sealed traits. The generation of `ConfigWriter`s is done by Magnolia.
  */
object writer {
  type Typeclass[A] = ConfigWriter[A]

  def join[A: ProductHint](ctx: CaseClass[ConfigWriter, A]): ConfigWriter[A] =
    MagnoliaConfigWriter.join(ctx)

  def split[A: ClassTag: CoproductHint](ctx: SealedTrait[ConfigWriter, A]): ConfigWriter[A] =
    MagnoliaConfigWriter.split(ctx)

  implicit def exportWriter[A]: Exported[ConfigWriter[A]] = macro ExportedMagnolia.exportedMagnolia[ConfigWriter, A]
}
