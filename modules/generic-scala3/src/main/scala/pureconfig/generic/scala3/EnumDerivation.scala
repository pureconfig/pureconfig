package pureconfig
package generic
package scala3

import scala.deriving.Mirror

import pureconfig.generic.derivation._

private[scala3] EnumDerivation {
  inline def deriveEnumerationReader[A: Mirror.SumOf](transformName: String => String): ConfigReader[A] =
    (new EnumConfigReaderDerivation(transformName) {}).EnumConfigReader.derived[A]

  inline def deriveEnumerationReader[A: Mirror.SumOf]: ConfigReader[A] =
    EnumConfigReaderDerivation.Default.EnumConfigReader.derived[A]

  inline def deriveEnumerationWriter[A: Mirror.SumOf](transformName: String => String): ConfigWriter[A] =
    (new EnumConfigWriterDerivation(transformName) {}).EnumConfigWriter.derived[A]

  inline def deriveEnumerationWriter[A: Mirror.SumOf]: ConfigWriter[A] =
    EnumConfigWriterDerivation.Default.EnumConfigWriter.derived[A]

  inline def deriveEnumerationConvert[A: Mirror.SumOf]: ConfigConvert[A] =
    EnumConfigConvertDerivation.Default.EnumConfigConvert.derived[A]

  inline def deriveEnumerationConvert[A: Mirror.SumOf](transformName: String => String): ConfigConvert[A] = {
    object Instance
        extends EnumConfigConvertDerivation,
          EnumConfigReaderDerivation(transformName),
          EnumConfigWriterDerivation(transformName)

    Instance.EnumConfigConvert.derived[A]
  }
}
