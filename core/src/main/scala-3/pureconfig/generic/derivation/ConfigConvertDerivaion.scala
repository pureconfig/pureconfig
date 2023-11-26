package pureconfig
package generic
package derivation

import scala.deriving.Mirror

import pureconfig.ConfigConvert
import pureconfig.generic.{CoproductHint, ProductHint}

object convert extends ConfigWriterDerivation, ConfigReaderDerivation, DefaultDerivationConfig:
  inline def deriveConvert[A](using m: Mirror.Of[A], ph: ProductHint[A], cph: CoproductHint[A]): ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])

  object syntax:
    extension (c: ConfigConvert.type)
      inline def derived[A](using Mirror.Of[A], ProductHint[A], CoproductHint[A]): ConfigConvert[A] = deriveConvert[A]
