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
      case given Mirror.Of[A] => deriveWriterWithMirror[A]
      case _ => deriveAnyValOrFail[A]
    }

  private[scala3] inline def summonConfigWriter[A]: ConfigWriter[A] =
    summonFrom {
      case writer: ConfigWriter[A] => writer
      case given Mirror.Of[A] => deriveWriterWithMirror[A]
      case _ => deriveAnyValOrFail[A]
    }

  private inline def deriveWriterWithMirror[A](using m: Mirror.Of[A]): ConfigWriter[A] =
    inline m match {
      case pm: Mirror.ProductOf[A] => deriveProductWriter[A](using pm, summonInline[ProductHint[A]])
      case sm: Mirror.SumOf[A] => deriveSumWriter[A](using sm, summonInline[CoproductHint[A]])
    }

  private inline def deriveAnyValOrFail[A]: ConfigWriter[A] =
    inline if (AnyValDerivationMacros.isAnyVal[A]) AnyValDerivationMacros.unsafeDeriveAnyValWriter[A]
    else error("Cannot derive ConfigWriter for: " + typeName[A])
}

object HintsAwareConfigWriterDerivation extends HintsAwareConfigWriterDerivation
