package pureconfig.module.magnolia.semiauto

import scala.language.experimental.macros
import scala.reflect.ClassTag

import magnolia._
import pureconfig.generic.{CoproductHint, ProductHint}
import pureconfig.module.magnolia.{EnumerationConfigWriterBuilder, MagnoliaConfigWriter}
import pureconfig.{ConfigFieldMapping, ConfigWriter, KebabCase, PascalCase}

/** An object that, when imported, provides methods for deriving `ConfigWriter` instances on demand for value classes,
  * tuples, case classes and sealed traits. The generation of `ConfigWriter`s is done by Magnolia.
  */
object writer {
  type Typeclass[A] = ConfigWriter[A]

  def combine[A: ProductHint](ctx: CaseClass[ConfigWriter, A]): ConfigWriter[A] =
    MagnoliaConfigWriter.combine(ctx)

  def dispatch[A: ClassTag: CoproductHint](ctx: SealedTrait[ConfigWriter, A]): ConfigWriter[A] =
    MagnoliaConfigWriter.dispatch(ctx)

  def deriveWriter[A]: ConfigWriter[A] = macro Magnolia.gen[A]

  /** Derive a `ConfigWriter` for a sealed family of case objects where each type is encoded as the kebab-case
    * representation of the type name.
    */
  def deriveEnumerationWriter[A: EnumerationConfigWriterBuilder]: ConfigWriter[A] =
    deriveEnumerationWriter[A](ConfigFieldMapping(PascalCase, KebabCase))

  /** Derive a `ConfigWriter` for a sealed family of case objects where each type is encoded with the `transformName`
    * function applied to the type name.
    */
  def deriveEnumerationWriter[A](
      transformName: String => String
  )(implicit builder: EnumerationConfigWriterBuilder[A]): ConfigWriter[A] = builder.build(transformName)
}
