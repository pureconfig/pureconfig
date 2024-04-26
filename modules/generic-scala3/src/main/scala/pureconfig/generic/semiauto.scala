package pureconfig.generic

import scala3._

object semiauto {
  export HintsAwareConfigReaderDerivation.deriveReader
  export HintsAwareConfigWriterDerivation.deriveWriter
  export HintsAwareConfigConvertDerivation.deriveConvert
}
