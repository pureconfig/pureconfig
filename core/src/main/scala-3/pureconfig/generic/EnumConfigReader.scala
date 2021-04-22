package pureconfig
package generic

import scala.deriving.Mirror

trait EnumConfigReader[A] extends ConfigReader[A]
object EnumConfigReader {
  inline def derived[A](using inline m: Mirror.SumOf[A]): EnumConfigReader[A] =
    deriveForEnum[A](ConfigFieldMapping(PascalCase, KebabCase))
}