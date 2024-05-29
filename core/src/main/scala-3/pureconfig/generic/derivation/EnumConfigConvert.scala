package pureconfig
package generic
package derivation

import scala.deriving.Mirror

import com.typesafe.config.ConfigValue

trait EnumConfigConvert[A] extends ConfigConvert[A]

object EnumConfigConvert {
  inline def derived[A: Mirror.SumOf]: EnumConfigConvert[A] =
    new EnumConfigConvert[A] {
      val reader = EnumConfigReaderDerivation.Default.EnumConfigReader.derived[A]
      val writer = EnumConfigWriterDerivation.Default.EnumConfigWriter.derived[A]

      def from(cur: ConfigCursor): ConfigReader.Result[A] = reader.from(cur)
      def to(a: A): ConfigValue = writer.to(a)
    }
}
