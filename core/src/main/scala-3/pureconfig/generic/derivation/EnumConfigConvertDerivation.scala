package pureconfig
package generic
package derivation

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

import com.typesafe.config.ConfigValue

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.derivation.Utils.*

type EnumConfigConvert[A] = EnumConfigConvertDerivation.Default.EnumConfigConvert[A]

trait EnumConfigConvertDerivation extends EnumConfigReaderDerivation, EnumConfigWriterDerivation {
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
  private lazy val defaultMapping = ConfigFieldMapping(PascalCase, KebabCase)

  object Default
      extends EnumConfigConvertDerivation,
        EnumConfigReaderDerivation(defaultMapping),
        EnumConfigWriterDerivation(defaultMapping)
}
