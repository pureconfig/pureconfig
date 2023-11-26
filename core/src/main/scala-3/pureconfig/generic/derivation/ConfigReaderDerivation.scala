package pureconfig
package generic
package derivation

import scala.deriving.Mirror

import pureconfig.generic.{CoproductHint, ProductHint}

trait ConfigReaderDerivation extends CoproductConfigReaderDerivation, ProductConfigReaderDerivation:
  inline def deriveReader[A](using m: Mirror.Of[A], ph: ProductHint[A], cph: CoproductHint[A]): ConfigReader[A] =
    inline m match
      case given Mirror.ProductOf[A] => deriveProductReader[A]
      case given Mirror.SumOf[A] => deriveSumReader[A]

object reader extends ConfigReaderDerivation, DefaultDerivationConfig:
  object syntax:
    extension (c: ConfigReader.type)
      inline def derived[A](using Mirror.Of[A], ProductHint[A], CoproductHint[A]): ConfigReader[A] = deriveReader[A]
