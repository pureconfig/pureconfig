package pureconfig
package generic
package derivation

import scala.deriving.Mirror

import pureconfig.generic.{CoproductHint, ProductHint}

trait ConfigWriterDerivation
    extends CoproductConfigWriterDerivation,
      ProductConfigWriterDerivation,
      DefaultDerivationConfig:
  extension (c: ConfigWriter.type)
    inline def derived[A](using m: Mirror.Of[A], ph: ProductHint[A], cph: CoproductHint[A]): ConfigWriter[A] =
      inline m match
        case given Mirror.ProductOf[A] => derivedProduct[A]
        case given Mirror.SumOf[A] => derivedSum[A]

object writer extends ConfigWriterDerivation
