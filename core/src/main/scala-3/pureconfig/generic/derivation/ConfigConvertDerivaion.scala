package pureconfig
package generic
package derivation

import scala.deriving.Mirror

object convert extends ConfigWriterDerivation, ConfigReaderDerivation:
  inline def deriveConvert[A](using Mirror.Of[A], ProductHint[A], CoproductHint[A]): ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])

  object syntax:
    extension (c: ConfigConvert.type)
      inline def derived[A](using Mirror.Of[A], ProductHint[A], CoproductHint[A]): ConfigConvert[A] = deriveConvert[A]
