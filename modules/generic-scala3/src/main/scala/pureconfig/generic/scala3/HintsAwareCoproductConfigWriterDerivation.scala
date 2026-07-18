package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

import com.typesafe.config.ConfigValue

import pureconfig.generic.derivation.Utils

trait HintsAwareCoproductConfigWriterDerivation { self: HintsAwareConfigWriterDerivation =>
  inline def deriveSumWriter[A](using m: Mirror.SumOf[A], ch: CoproductHint[A]): ConfigWriter[A] =
    new ConfigWriter[A] {
      val labels = Utils.transformedSumLabels(identity, descend = false).toVector
      val writers = summonAllConfigWriters[m.MirroredElemTypes].toVector

      def to(a: A): ConfigValue = {
        val n = m.ordinal(a)
        val writer = writers(n).asInstanceOf[ConfigWriter[Any]]

        if (isSum[m.MirroredElemTypes](n)) writer.to(a)
        else summon[CoproductHint[A]].to(writer.to(a), labels(n))
      }
    }

  private inline def isSum[T <: Tuple](n: Int): Boolean =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        if (n == 0) summonFrom {
          case _: Mirror.SumOf[`h`] => true
          case _ => false
        }
        else isSum[t](n - 1)
      case _: EmptyTuple => false
    }

  private inline def summonAllConfigWriters[T <: Tuple]: List[ConfigWriter[?]] =
    inline erasedValue[T] match {
      case _: (h *: t) => summonConfigWriter[h] :: summonAllConfigWriters[t]
      case _: EmptyTuple => Nil
    }
}
