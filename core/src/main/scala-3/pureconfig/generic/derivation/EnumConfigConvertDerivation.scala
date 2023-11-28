package pureconfig
package generic
package derivation

import scala.deriving.Mirror

trait EnumConfigConvert[A] extends ConfigConvert[A]

object EnumConfigConvert:
  import EnumConfigReader.*, EnumConfigWriter.*

  inline def derived[A](using Mirror.SumOf[A], EnumHint[A]): ConfigConvert[A] = deriveEnumerationConvert[A]

  inline def deriveEnumerationConvert[A](transformName: String => String)(using Mirror.SumOf[A]): ConfigConvert[A] =
    given EnumHint[A] = EnumHint(ConfigFieldMapping(transformName))
    deriveEnumerationConvert[A]

  inline def deriveEnumerationConvert[A](using Mirror.SumOf[A], EnumHint[A]): ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveEnumerationReader[A], deriveEnumerationWriter[A])
