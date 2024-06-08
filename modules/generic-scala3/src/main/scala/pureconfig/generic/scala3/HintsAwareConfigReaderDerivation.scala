package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

trait HintsAwareConfigReaderDerivation
    extends HintsAwareCoproductConfigReaderDerivation,
      HintsAwareProductConfigReaderDerivation {
  inline def deriveReader[A <: AnyVal]: ConfigReader[A] = AnyValDerivationMacros.deriveAnyValReader[A]

  inline def deriveReader[A](using m: Mirror.Of[A]): ConfigReader[A] =
    inline m match {
      case pm: Mirror.ProductOf[A] => deriveProductReader[A](using pm, summonInline[ProductHint[A]])
      case sm: Mirror.SumOf[A] => deriveSumReader[A](using sm, summonInline[CoproductHint[A]])
    }

  protected inline def summonConfigReader[A]: ConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A] => reader
      case given Mirror.Of[A] => deriveReader[A]
    }
}

object HintsAwareConfigReaderDerivation extends HintsAwareConfigReaderDerivation
