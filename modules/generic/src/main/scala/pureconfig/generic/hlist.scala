package pureconfig.generic

import pureconfig.{ ConfigReader, ConfigWriter }

/**
 * An object that, when imported, provides readers and writers for shapeless `HList` instances.
 */
object hlist {
  implicit def hListReader[HL](implicit seqReader: SeqShapedReader[HL]): ConfigReader[HL] = seqReader
  implicit def hListWriter[HL](implicit seqWriter: SeqShapedWriter[HL]): ConfigWriter[HL] = seqWriter
}
