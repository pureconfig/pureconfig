package pureconfig
package generic
package derivation

import scala.deriving.Mirror

trait EnumConfigConvert[A] extends ConfigConvert[A]

object EnumConfigConvert:
  import EnumConfigReader.*, EnumConfigWriter.*

  inline def derived[A](using Mirror.SumOf[A], EnumHint[A]): ConfigConvert[A] = deriveEnumConvert[A]

  inline def deriveEnumConvert[A](using Mirror.SumOf[A], EnumHint[A]): ConfigConvert[A] =
    ConfigConvert.fromReaderAndWriter(deriveEnumerationReader[A], deriveEnumerationWriter[A])
