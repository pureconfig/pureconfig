package pureconfig.generic

import pureconfig.{ ConfigReader, ConfigWriter }

object hlist {
  implicit def hListReader[HL](implicit seqReader: SeqShapedReader[HL]): ConfigReader[HL] = seqReader
  implicit def hListWriter[HL](implicit seqWriter: SeqShapedWriter[HL]): ConfigWriter[HL] = seqWriter
}
