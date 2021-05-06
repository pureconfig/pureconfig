package pureconfig
package generic
package derivation

import scala.deriving.Mirror

import pureconfig.generic.{CoproductHint, ProductHint}

class ConfigReaderDerivation(coproductHint: CoproductHint[?], productHint: ProductHint[?])
    extends CoproductConfigReaderDerivation(coproductHint)
    with ProductConfigReaderDerivation(productHint) {

  trait DerivedConfigReader[A] extends ConfigReader[A]
  object DerivedConfigReader {
    inline def derived[A](using m: Mirror.Of[A]): DerivedConfigReader[A] =
      inline m match {
        case given Mirror.ProductOf[A] => ProductConfigReader.derived
        case given Mirror.SumOf[A] => CoproductConfigReader.derived
      }
  }

  def withCoproductHint(coproductHint: CoproductHint[?]) =
    new ConfigReaderDerivation(
      coproductHint,
      productHint
    )

  def withProductHint(productHint: ProductHint[?]) =
    new ConfigReaderDerivation(
      coproductHint,
      productHint
    )

  extension (c: ConfigReader.type)
    inline def derived[A](using m: Mirror.Of[A]): ConfigReader[A] =
      DerivedConfigReader.derived
}

object ConfigReaderDerivation {
  object Default
      extends ConfigReaderDerivation(
        CoproductHint.default[Any],
        ProductHint.default[Any]
      )
}

val default = ConfigReaderDerivation.Default
