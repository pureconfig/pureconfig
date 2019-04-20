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

  final def deriveEnumerationReader[A](
    implicit
    readerBuilder: Lazy[EnumerationConfigReaderBuilder[A]]): ConfigReader[A] =
    deriveEnumerationReader(_.toLowerCase)
  final def deriveEnumerationWriter[A](
    implicit
    writerBuilder: Lazy[EnumerationConfigWriterBuilder[A]]): ConfigWriter[A] =
    deriveEnumerationWriter(_.toLowerCase)
  final def deriveEnumerationConvert[A](
    implicit
    readerBuilder: Lazy[EnumerationConfigReaderBuilder[A]],
    writerBuilder: Lazy[EnumerationConfigWriterBuilder[A]]): ConfigConvert[A] =
    deriveEnumerationConvert(_.toLowerCase)

  final def deriveEnumerationReader[A](transformName: String => String)(
    implicit
    readerBuilder: Lazy[EnumerationConfigReaderBuilder[A]]): ConfigReader[A] = readerBuilder.value.build(transformName)
  final def deriveEnumerationWriter[A](transformName: String => String)(
    implicit
    writerBuilder: Lazy[EnumerationConfigWriterBuilder[A]]): ConfigWriter[A] = writerBuilder.value.build(transformName)
  final def deriveEnumerationConvert[A](transformName: String => String)(
    implicit
    readerBuilder: Lazy[EnumerationConfigReaderBuilder[A]],
    writerBuilder: Lazy[EnumerationConfigWriterBuilder[A]]): ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(
      Derivation.Successful(readerBuilder.value.build(transformName)),
      Derivation.Successful(writerBuilder.value.build(transformName)))
}
