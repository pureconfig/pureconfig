package pureconfig
package generic
package derivation

import scala.compiletime.*
import scala.deriving.Mirror
import com.typesafe.config.ConfigValue

trait CoproductConfigWriterDerivation:
  self: ConfigWriterDerivation =>

  inline def deriveSumWriter[A](using m: Mirror.SumOf[A], ch: CoproductHint[A], ph: ProductHint[A]): ConfigWriter[A] =
    new ConfigWriter[A]:
      val labels = Labels.transformed[m.MirroredElemLabels](identity)
      val writers = summonAllConfigWriters[m.MirroredElemTypes]

      def to(a: A): ConfigValue =
        val n = m.ordinal(a)
        val label = labels(n)
        val writer = writers(n).asInstanceOf[ConfigWriter[Any]]

        summon[CoproductHint[A]].to(writer.to(a), label)

  private inline def summonAllConfigWriters[T <: Tuple]: List[ConfigWriter[?]] =
    inline erasedValue[T] match
      case _: (h *: t) => summonConfigWriter[h] :: summonAllConfigWriters[t]
      case _: EmptyTuple => Nil

  private inline def summonConfigWriter[A]: ConfigWriter[A] =
    summonFrom:
      case writer: ConfigWriter[A] => writer
      case m: Mirror.Of[A] =>
        deriveWriter[A](using m, summonInline[ProductHint[A]], summonInline[CoproductHint[A]])
