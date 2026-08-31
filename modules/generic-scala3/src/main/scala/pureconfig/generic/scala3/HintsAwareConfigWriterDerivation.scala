package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

import derivation.Utils._

trait HintsAwareConfigWriterDerivation
    extends HintsAwareCoproductConfigWriterDerivation,
      HintsAwareProductConfigWriterDerivation {
  inline def deriveWriter[A]: ConfigWriter[A] =
    summonFrom {
      case ma: Mirror.Of[A] => deriveWriterWithMirror[A](using ma, DerivationFlow.default)
      case _ => deriveAnyValOrFail[A](using DerivationFlow.default)
    }

  private[scala3] inline def summonConfigWriter[A](using inline df: DerivationFlow): ConfigWriter[A] =
    summonFrom {
      case writer: ConfigWriter[A] => writer
      case given Mirror.Of[A] => deriveWriterWithMirror[A]
      case _ => deriveAnyValOrFail[A]
    }

  private inline def deriveWriterWithMirror[A](using m: Mirror.Of[A], inline df: DerivationFlow): ConfigWriter[A] =
    inline m match {
      case pm: Mirror.ProductOf[A] => deriveProductWriter[A](using pm, summonInline[ProductHint[A]], df.throughProduct)
      case sm: Mirror.SumOf[A] if df.allowAutoSum => deriveSumWriter[A](using sm, summonInline[CoproductHint[A]])
      case _ => error("Cannot derive ConfigWriter for: " + typeName[A])
    }

  private inline def deriveAnyValOrFail[A](using inline df: DerivationFlow): ConfigWriter[A] =
    inline if (AnyValDerivationMacros.isAnyVal[A]) AnyValDerivationMacros.unsafeDeriveAnyValWriter[A]
    else error("Cannot derive ConfigWriter for: " + typeName[A])
}

object HintsAwareConfigWriterDerivation extends HintsAwareConfigWriterDerivation
