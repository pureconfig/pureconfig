package pureconfig
package generic
package derivation

import scala.deriving.Mirror

import pureconfig.generic.{CoproductHint, ProductHint}

trait ConfigWriterDerivation extends CoproductConfigWriterDerivation, ProductConfigWriterDerivation:
  inline def deriveWriter[A](using m: Mirror.Of[A], ph: ProductHint[A], cph: CoproductHint[A]): ConfigWriter[A] =
    inline m match
      case given Mirror.ProductOf[A] => deriveProductWriter[A]
      case given Mirror.SumOf[A] => deriveSumWriter[A]

object writer extends ConfigWriterDerivation, DefaultDerivationConfig:
  object syntax:
    extension (c: ConfigWriter.type)
      inline def derived[A](using Mirror.Of[A], ProductHint[A], CoproductHint[A]): ConfigWriter[A] = deriveWriter[A]
