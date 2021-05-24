package pureconfig
package generic
package derivation

import scala.deriving.Mirror
import pureconfig.generic.derivation.ConfigReaderDerivation

trait ConfigReaderDerivation extends CoproductConfigReaderDerivation with ProductConfigReaderDerivation {
  extension (c: ConfigReader.type) {
    inline def derived[A](using m: Mirror.Of[A]): ConfigReader[A] =
      inline m match {
        case given Mirror.ProductOf[A] => derivedProduct
        case given Mirror.SumOf[A] => derivedSum
      }
  }
}

object ConfigReaderDerivation {
  object Default
      extends ConfigReaderDerivation
      with CoproductConfigReaderDerivation(ConfigFieldMapping(PascalCase, KebabCase), "type")
      with ProductConfigReaderDerivation(ConfigFieldMapping(CamelCase, KebabCase))
}

val default = ConfigReaderDerivation.Default
