package pureconfig
package generic
package derivation

import scala.compiletime.{constValue, erasedValue, error, summonInline}
import scala.deriving.Mirror

import com.typesafe.config.{ConfigValue, ConfigValueFactory}

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.derivation.WidenType.widen

trait EnumConfigWriter[A] extends ConfigWriter[A]

object EnumConfigWriter:
  inline def derived[A](using Mirror.SumOf[A], EnumHint[A]): EnumConfigWriter[A] =
    deriveEnumerationWriter

  inline def deriveEnumerationWriter[A](using Mirror.SumOf[A], EnumHint[A]): EnumConfigWriter[A] =
    deriveEnumerationWriter(summonInline[EnumHint[A]].transformName)

  inline def deriveEnumerationWriter[A](
      transformName: String => String
  )(using m: Mirror.SumOf[A]): EnumConfigWriter[A] =
    new EnumConfigWriter[A]:
      assertIsEnum[m.MirroredElemTypes]
      val labels = Labels.transformed[m.MirroredElemLabels](transformName)

      def to(a: A): ConfigValue = ConfigValueFactory.fromAnyRef(labels(m.ordinal(a)))

  private inline def assertIsEnum[T <: Tuple]: Unit =
    inline erasedValue[T] match
      case _: (h *: t) =>
        inline summonInline[Mirror.Of[h]] match
          case m: Mirror.Singleton => assertIsEnum[t]
          case _ => error("Enums cannot include parameterized cases.")
      case _: EmptyTuple => ()
