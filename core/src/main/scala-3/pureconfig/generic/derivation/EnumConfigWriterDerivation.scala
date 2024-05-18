package pureconfig
package generic
package derivation

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

import com.typesafe.config.{ConfigValue, ConfigValueFactory}

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.derivation.Utils.*

type EnumConfigWriter[A] = EnumConfigWriterDerivation.Default.EnumConfigWriter[A]

trait EnumConfigWriterDerivation(transformName: String => String) {

  trait EnumConfigWriter[A] extends ConfigWriter[A]

  object EnumConfigWriter {
    inline def derived[A](using m: Mirror.SumOf[A]): EnumConfigWriter[A] =
      new EnumConfigWriter[A] {
        assertIsEnum[m.MirroredElemTypes]
        val labels = transformedLabels[A](transformName).toVector

        def to(a: A): ConfigValue =
          ConfigValueFactory.fromAnyRef(labels(m.ordinal(a)))
      }
  }

  private inline def assertIsEnum[T <: Tuple]: Unit =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        inline summonInline[Mirror.Of[h]] match {
          case m: Mirror.Singleton => assertIsEnum[t]
          case _ => error("Enums cannot include parameterized cases.")
        }
      case _: EmptyTuple => ()
    }
}

object EnumConfigWriterDerivation {
  object Default extends EnumConfigWriterDerivation(ConfigFieldMapping(PascalCase, KebabCase))
}
