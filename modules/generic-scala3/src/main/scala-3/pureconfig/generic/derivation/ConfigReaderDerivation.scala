package pureconfig
package generic
package derivation

import scala.deriving.Mirror

trait ConfigReaderDerivation extends CoproductConfigReaderDerivation, ProductConfigReaderDerivation:
  inline def deriveReader[A](using m: Mirror.Of[A], ph: ProductHint[A], cph: CoproductHint[A]): ConfigReader[A] =
    inline m match
      case given Mirror.ProductOf[A] => deriveProductReader[A]
      case given Mirror.SumOf[A] => deriveSumReader[A]

object reader extends ConfigReaderDerivation
