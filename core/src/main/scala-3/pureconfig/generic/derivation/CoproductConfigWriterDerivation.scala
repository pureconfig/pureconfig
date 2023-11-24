package pureconfig
package generic
package derivation

import scala.collection.JavaConverters.*
import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror

import com.typesafe.config.{ConfigValue, ConfigValueFactory}

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.CoproductHint
import pureconfig.generic.derivation.ConfigReaderDerivation
import pureconfig.generic.derivation.WidenType.widen
import pureconfig.generic.error.InvalidCoproductOption

trait CoproductConfigWriterDerivation:
  self: ConfigWriterDerivation =>

  inline def derivedSum[A](using m: Mirror.SumOf[A], ch: CoproductHint[A], ph: ProductHint[A]): ConfigWriter[A] =
    new ConfigWriter[A]:
      def to(a: A): ConfigValue =
        val labels = Labels.transformed[m.MirroredElemLabels](identity)
        val writers = labels.zip(summonAllConfigWriters[m.MirroredElemTypes, A]).toMap

        val values = labels.map: label =>
          val writer = writers(label)
          summon[CoproductHint[A]].to(writer.to(a), label)

        ConfigValueFactory.fromIterable(values.asJava)

  inline def summonAllConfigWriters[T <: Tuple, A]: List[ConfigWriter[A]] =
    inline erasedValue[T] match
      case _: (h *: t) => (summonConfigWriter[h] :: summonAllConfigWriters[t, A]).asInstanceOf[List[ConfigWriter[A]]]
      case _: EmptyTuple => Nil

  inline def summonConfigWriter[A]: ConfigWriter[A] =
    summonFrom:
      case writer: ConfigWriter[A] => writer
      case m: Mirror.Of[A] =>
        ConfigWriter.derived[A](using m, summonInline[ProductHint[A]], summonInline[CoproductHint[A]])
