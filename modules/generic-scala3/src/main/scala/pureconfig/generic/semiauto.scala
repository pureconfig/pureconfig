package pureconfig.generic

import scala.deriving.Mirror

import pureconfig.ConfigConvert
import pureconfig.generic.derivation._

import scala3._

object semiauto {
  export HintsAwareConfigReaderDerivation.deriveReader
  export HintsAwareConfigWriterDerivation.deriveWriter
  export EnumDerivation._

  inline def deriveConvert[A <: AnyVal]: ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])

  inline def deriveConvert[A: Mirror.Of]: ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])
}
