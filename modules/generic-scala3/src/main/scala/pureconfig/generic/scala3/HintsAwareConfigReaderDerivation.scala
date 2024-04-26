package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

trait HintsAwareConfigReaderDerivation
    extends HintsAwareCoproductConfigReaderDerivation,
      HintsAwareProductConfigReaderDerivation {
  inline def deriveReader[A](using m: Mirror.Of[A], ph: ProductHint[A], cph: CoproductHint[A]): ConfigReader[A] =
    inline m match {
      case pm: Mirror.ProductOf[A] => deriveProductReader[A](using pm, ph)
      case sm: Mirror.SumOf[A] => deriveSumReader[A](using sm, cph)
    }

  protected inline def summonConfigReader[A]: ConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A] => reader
      case m: Mirror.Of[A] =>
        deriveReader[A](using m, summonInline[ProductHint[A]], summonInline[CoproductHint[A]])
    }
}

object HintsAwareConfigReaderDerivation extends HintsAwareConfigReaderDerivation
