package pureconfig.module.magnolia.semiauto

import scala.language.experimental.macros

import magnolia._

import pureconfig.generic.{CoproductHint, ProductHint}
import pureconfig.module.magnolia.{EnumerationConfigReaderBuilder, MagnoliaConfigReader}
import pureconfig.{ConfigFieldMapping, ConfigReader, KebabCase, PascalCase}

/** An object that, when imported, provides methods for deriving `ConfigReader` instances on demand for value classes,
  * tuples, case classes and sealed traits. The generation of `ConfigReader`s is done by Magnolia.
  */
object reader {
  type Typeclass[A] = ConfigReader[A]

  def combine[A: ProductHint](ctx: CaseClass[ConfigReader, A]): ConfigReader[A] =
    MagnoliaConfigReader.combine(ctx)

  def dispatch[A: CoproductHint](ctx: SealedTrait[ConfigReader, A]): ConfigReader[A] =
    MagnoliaConfigReader.dispatch(ctx)

  def deriveReader[A]: ConfigReader[A] = macro Magnolia.gen[A]

  /** Derive a `ConfigReader` for a sealed family of case objects where each type is encoded as the kebab-case
    * representation of the type name.
    */
  def deriveEnumerationReader[A: EnumerationConfigReaderBuilder]: ConfigReader[A] =
    deriveEnumerationReader[A](ConfigFieldMapping(PascalCase, KebabCase))

  /** Derive a `ConfigReader` for a sealed family of case objects where each type is encoded with the `transformName`
    * function applied to the type name.
    */
  def deriveEnumerationReader[A](
      transformName: String => String
  )(implicit builder: EnumerationConfigReaderBuilder[A]): ConfigReader[A] = builder.build(transformName)
}
