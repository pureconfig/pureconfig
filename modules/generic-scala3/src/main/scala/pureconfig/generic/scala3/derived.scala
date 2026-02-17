package pureconfig
package generic
package scala3

import HintsAwareConfigReaderDerivation.deriveReader
import HintsAwareConfigWriterDerivation.deriveWriter
import scala.deriving.Mirror

extension (c: ConfigReader.type)
  inline def derived[A: Mirror.Of: ProductHint: CoproductHint]: ConfigReader[A] = deriveReader

extension (c: ConfigWriter.type)
  inline def derived[A: Mirror.Of: ProductHint: CoproductHint]: ConfigWriter[A] = deriveWriter

extension (c: ConfigConvert.type)
  inline def derived[A: Mirror.Of: ProductHint: CoproductHint]: ConfigConvert[A] = semiauto.deriveConvert
