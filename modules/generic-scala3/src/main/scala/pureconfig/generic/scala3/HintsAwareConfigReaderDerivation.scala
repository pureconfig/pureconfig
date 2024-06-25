package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

import derivation.Utils._

trait HintsAwareConfigReaderDerivation
    extends HintsAwareCoproductConfigReaderDerivation,
      HintsAwareProductConfigReaderDerivation {
  inline def deriveReader[A <: AnyVal]: ConfigReader[A] = AnyValDerivationMacros.unsafeDeriveAnyValReader[A](this)

  inline def deriveReader[A](using m: Mirror.Of[A]): ConfigReader[A] =
    inline m match {
      case pm: Mirror.ProductOf[A] => deriveProductReader[A](using pm, summonInline[ProductHint[A]])
      case sm: Mirror.SumOf[A] => deriveSumReader[A](using sm, summonInline[CoproductHint[A]])
    }

  private[scala3] inline def summonConfigReader[A]: ConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A] => reader
      case given Mirror.Of[A] => deriveReader[A]
      case _ =>
        inline if (AnyValDerivationMacros.isAnyVal[A]) AnyValDerivationMacros.unsafeDeriveAnyValReader[A](this)
        else error("Cannot derive ConfigReader for " + typeName[A])
    }

}

object HintsAwareConfigReaderDerivation extends HintsAwareConfigReaderDerivation
