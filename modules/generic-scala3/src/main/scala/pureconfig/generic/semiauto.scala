package pureconfig.generic

import scala.deriving.Mirror

import pureconfig._
import pureconfig.generic.derivation._

import scala3._

object semiauto {
  export HintsAwareConfigReaderDerivation.deriveReader
  export HintsAwareConfigWriterDerivation.deriveWriter
  export EnumDerivation._

  inline def deriveConvert[A: Mirror.Of]: ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])

  extension (c: ConfigReader.type)
    inline def derived[A: Mirror.Of: ProductHint: CoproductHint]: ConfigReader[A] = deriveReader[A]
}
