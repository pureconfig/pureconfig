package pureconfig
package generic
package derivation

import scala.compiletime.summonFrom
import scala.deriving.Mirror

@deprecated(
  "Custom derivation is deprecated in pureconfig-core. If you only need the default behavior, please use the default `derives` behavior. If you need configuration please use the `pureconfig-generic-scala3` module instead.",
  "0.17.7"
)
trait ConfigReaderDerivation extends CoproductConfigReaderDerivation with ProductConfigReaderDerivation {
  extension (c: ConfigReader.type) {
    inline def derived[A](using m: Mirror.Of[A]): ConfigReader[A] =
      deriveConfigReader[A]
  }

  inline def deriveConfigReader[A](using m: Mirror.Of[A]): ConfigReader[A] =
    inline m match {
      case given Mirror.ProductOf[A] => derivedProduct
      case given Mirror.SumOf[A] => derivedSum
    }

  /** Summons a `ConfigReader` for a given type `A`. It first tries to find an existing given instance of
    * `ConfigReader[A]`. If none is found, it tries to derive one using this `ConfigReaderDerivation` instance. This
    * method differs from `derived` in that the latter doesn't try to find an existing instance first.
    */
  protected inline def summonConfigReader[A] = summonFrom {
    case reader: ConfigReader[A] => reader
    case given Mirror.Of[A] => ConfigReader.derived[A]
  }
}

@deprecated("Derivation of ConfigReaders using `derives` is now supported without an import.", "0.17.7")
object ConfigReaderDerivation {
  object Default
      extends ConfigReaderDerivation
      with CoproductConfigReaderDerivation(ConfigFieldMapping(PascalCase, KebabCase), "type")
      with ProductConfigReaderDerivation(ConfigFieldMapping(CamelCase, KebabCase))
}

@deprecated("Derivation of ConfigReaders using `derives` is now supported without an import.", "0.17.7")
val default = ConfigReaderDerivation.Default
