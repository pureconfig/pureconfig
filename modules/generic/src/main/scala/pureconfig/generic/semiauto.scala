package pureconfig.generic

import pureconfig._
import shapeless._

/**
 * An object that provides methods for deriving `ConfigReader` and `ConfigWriter` instances on demand for value
 * classes, tuples, case classes and sealed traits.
 */
object semiauto {
  final def deriveReader[A](implicit reader: Lazy[DerivedConfigReader[A]]): ConfigReader[A] = reader.value
  final def deriveWriter[A](implicit writer: Lazy[DerivedConfigWriter[A]]): ConfigWriter[A] = writer.value

  final def deriveConvert[A](implicit reader: Lazy[DerivedConfigReader[A]], writer: Lazy[DerivedConfigWriter[A]]): ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(Derivation.Successful(reader.value), Derivation.Successful(writer.value))
}
