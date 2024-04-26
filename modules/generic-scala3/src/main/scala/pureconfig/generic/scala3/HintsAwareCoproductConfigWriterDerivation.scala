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
      val labels = Utils.transformedLabels(identity).toVector
      val writers = summonAllConfigWriters[m.MirroredElemTypes].toVector

      def to(a: A): ConfigValue = {
        val n = m.ordinal(a)
        val label = labels(n)
        val writer = writers(n).asInstanceOf[ConfigWriter[Any]]

        summon[CoproductHint[A]].to(writer.to(a), label)
      }
    }

  private inline def summonAllConfigWriters[T <: Tuple]: List[ConfigWriter[?]] =
    inline erasedValue[T] match {
      case _: (h *: t) => summonConfigWriter[h] :: summonAllConfigWriters[t]
      case _: EmptyTuple => Nil
    }
}
