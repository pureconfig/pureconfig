package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

import derivation.Utils.TypeName

trait HintsAwareConfigReaderDerivation
    extends HintsAwareCoproductConfigReaderDerivation,
      HintsAwareProductConfigReaderDerivation {
  inline def deriveReader[A <: AnyVal]: ConfigReader[A] = AnyValDerivationMacros.unsafeDeriveAnyValReader[A]

  inline def deriveReader[A](using m: Mirror.Of[A]): ConfigReader[A] =
    inline m match {
      case pm: Mirror.ProductOf[A] => deriveProductReader[A](using pm, summonInline[ProductHint[A]])
      case sm: Mirror.SumOf[A] => deriveSumReader[A](using sm, summonInline[CoproductHint[A]])
    }

  private[pureconfig] inline def summonConfigReader[A]: ConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A] => reader
      case given Mirror.Of[A] => deriveReader[A]
      case _ =>
        inline if AnyValDerivationMacros.isAnyVal[A]
        then AnyValDerivationMacros.unsafeDeriveAnyValReader[A]
        else error("Cannot derive ConfigReader for " + TypeName[A])
    }

}

object HintsAwareConfigReaderDerivation extends HintsAwareConfigReaderDerivation
