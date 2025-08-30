package pureconfig.generic

import scala.deriving.Mirror

import pureconfig.ConfigConvert
import pureconfig.generic.derivation._

import scala3._

object semiauto {
  export HintsAwareConfigReaderDerivation.{deriveReader, deriveReaderSemiauto}
  export HintsAwareConfigWriterDerivation.{deriveWriter, deriveWriterSemiauto}
  export EnumDerivation._

  inline def deriveConvert[A]: ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])

  inline def deriveConvertSemiauto[A]: ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReaderSemiauto[A], deriveWriterSemiauto[A])
}
