package pureconfig
package generic
package scala3

import scala.deriving.Mirror

trait HintsAwareConfigConvertDerivation extends HintsAwareConfigWriterDerivation, HintsAwareConfigReaderDerivation:
  inline def deriveConvert[A: Mirror.Of]: ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveReader[A], deriveWriter[A])

object HintsAwareConfigConvertDerivation extends HintsAwareConfigConvertDerivation
