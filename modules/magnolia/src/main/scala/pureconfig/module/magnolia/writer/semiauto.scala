package pureconfig.module.magnolia.writer

import scala.language.experimental.macros
import scala.reflect.ClassTag

import magnolia._
import pureconfig.ConfigWriter
import pureconfig.generic.{ CoproductHint, ProductHint }
import pureconfig.module.magnolia.MagnoliaConfigWriter

/**
 * An object that, when imported, provides methods for deriving `ConfigWriter` instances on demand for value classes,
 * tuples, case classes and sealed traits. The generation of `ConfigWriter`s is done by Magnolia.
 */
object semiauto {
  type Typeclass[A] = ConfigWriter[A]

  def combine[A: ProductHint](ctx: CaseClass[ConfigWriter, A]): ConfigWriter[A] =
    MagnoliaConfigWriter.combine(ctx)

  def dispatch[A: ClassTag: CoproductHint](ctx: SealedTrait[ConfigWriter, A]): ConfigWriter[A] =
    MagnoliaConfigWriter.dispatch(ctx)

  def deriveWriter[A]: ConfigWriter[A] = macro Magnolia.gen[A]
}
