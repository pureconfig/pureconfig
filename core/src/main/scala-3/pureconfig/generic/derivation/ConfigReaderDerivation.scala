package pureconfig
package generic
package derivation

import scala.deriving.Mirror

import pureconfig.generic.{CoproductHint, ProductHint}

trait ConfigReaderDerivation
    extends CoproductConfigReaderDerivation,
      ProductConfigReaderDerivation,
      DefaultDerivationConfig:
  extension (c: ConfigReader.type)
    inline def derived[A](using m: Mirror.Of[A], ph: ProductHint[A], cph: CoproductHint[A]): ConfigReader[A] =
      inline m match
        case given Mirror.ProductOf[A] => derivedProduct[A]
        case given Mirror.SumOf[A] => derivedSum[A]

object reader extends ConfigReaderDerivation

// TODO: move to converter
val default = reader
