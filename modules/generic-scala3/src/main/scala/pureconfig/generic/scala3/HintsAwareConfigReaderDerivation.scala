package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

import derivation.Utils._

trait HintsAwareConfigReaderDerivation
    extends HintsAwareCoproductConfigReaderDerivation,
      HintsAwareProductConfigReaderDerivation {
  inline def deriveReader[A]: ConfigReader[A] =
    summonFrom {
      case ma: Mirror.Of[A] => deriveReaderWithMirror[A](using ma, DerivationFlow.auto)
      case _ => deriveAnyValOrFail[A](using DerivationFlow.auto)
    }

  inline def deriveReaderSemiauto[A]: ConfigReader[A] =
    summonFrom {
      case ma: Mirror.Of[A] => deriveReaderWithMirror[A](using ma, DerivationFlow.semiauto)
      case _ => deriveAnyValOrFail[A](using DerivationFlow.semiauto)
    }

  private[scala3] inline def summonConfigReader[A](using inline df: DerivationFlow): ConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A] => reader
      case given Mirror.Of[A] => deriveReaderWithMirror[A]
      case _ => deriveAnyValOrFail[A]
    }

  private inline def deriveReaderWithMirror[A](using m: Mirror.Of[A], inline df: DerivationFlow): ConfigReader[A] =
    inline m match {
      case pm: Mirror.ProductOf[A] => deriveProductReader[A](using pm, summonInline[ProductHint[A]], df.throughProduct)
      case sm: Mirror.SumOf[A] if df.allowAutoSum => deriveSumReader[A](using sm, summonInline[CoproductHint[A]])
      case _ => error("Cannot derive ConfigReader for " + typeName[A])
    }

  private inline def deriveAnyValOrFail[A](using inline df: DerivationFlow): ConfigReader[A] =
    inline if (AnyValDerivationMacros.isAnyVal[A]) AnyValDerivationMacros.unsafeDeriveAnyValReader[A]
    else error("Cannot derive ConfigReader for " + typeName[A])
}

object HintsAwareConfigReaderDerivation extends HintsAwareConfigReaderDerivation
