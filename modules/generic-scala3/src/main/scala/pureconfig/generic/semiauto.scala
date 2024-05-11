package pureconfig.generic

import scala.deriving.Mirror

import pureconfig.ConfigConvert

import scala3._

object semiauto {
  export HintsAwareConfigReaderDerivation.deriveReader
  export HintsAwareConfigWriterDerivation.deriveWriter

  inline def deriveConvert[A: Mirror.Of]: ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])
}
