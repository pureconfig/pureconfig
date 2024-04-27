package pureconfig
package generic
package derivation

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.derivation.Utils._

import com.typesafe.config.ConfigValue

type EnumConfigConvert[A] = EnumConfigConvertDerivation.Default.EnumConfigConvert[A]

trait EnumConfigConvertDerivation(transformName: String => String) extends EnumConfigReaderDerivation, EnumConfigWriterDerivation {
  trait EnumConfigConvert[A] extends ConfigConvert[A]
  object EnumConfigConvert {
    inline def derived[A: Mirror.SumOf]: EnumConfigConvert[A] =
      new EnumConfigConvert[A] {
        val reader = EnumConfigReader.derived[A]
        val writer = EnumConfigWriter.derived[A]

        def from(cur: ConfigCursor): ConfigReader.Result[A] = reader.from(cur)
        def to(a: A): ConfigValue = writer.to(a)
      }
  }
}

object EnumConfigConvertDerivation {
    // parameterized trait Kek is indirectly implemented, needs to be implemented directly so that arguments can be passed ???
  object Default extends EnumConfigConvertDerivation(ConfigFieldMapping(PascalCase, KebabCase))
}
