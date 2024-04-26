package pureconfig
package generic
package scala3

import scala.collection.JavaConverters.given
import scala.compiletime._
import scala.compiletime.ops.int._
import scala.deriving.Mirror
import scala.quoted._

import com.typesafe.config.{ConfigValue, ConfigValueFactory}

import pureconfig.generic.derivation.Utils

trait HintsAwareProductConfigWriterDerivation { self: HintsAwareConfigWriterDerivation =>

  inline def deriveProductWriter[A](using pm: Mirror.ProductOf[A], ph: ProductHint[A]): ConfigWriter[A] =
    inline erasedValue[A] match {
      case _: Tuple =>
        new ConfigWriter[A] {
          def to(a: A): ConfigValue = {
            val values = writeTuple[pm.MirroredElemTypes, 0](a.asInstanceOf[Product])

            ConfigValueFactory.fromIterable(values.asJava)
          }
        }

      case _ =>
        new ConfigWriter[A] {
          def to(a: A): ConfigValue = {
            val labels = Utils.transformedLabels(identity).toVector
            val values = writeCaseClass[pm.MirroredElemTypes, 0, A](a.asInstanceOf[Product], labels)

            ConfigValueFactory.fromMap(values.toMap.asJava)
          }
        }
    }

  private inline def writeTuple[T <: Tuple, N <: Int](product: Product): List[ConfigValue] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val n = constValue[N]
        val value = product.productElement(n).asInstanceOf[h]
        val head = summonConfigWriter[h].to(value)
        val tail = writeTuple[t, N + 1](product)

        head :: tail

      case _: EmptyTuple => Nil
    }

  private inline def writeCaseClass[T <: Tuple, N <: Int, A: ProductHint](
      product: Product,
      labels: Vector[String]
  ): List[(String, ConfigValue)] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val n = constValue[N]
        def value[T] = product.productElement(n).asInstanceOf[T]

        val valueOpt = summonConfigWriter[h] match {
          case writer: WritesMissingKeys[h] => writer.toOpt(value)
          case writer => Some(writer.to(value))
        }

        val head = summon[ProductHint[A]].to(valueOpt, labels(n)).toList
        val tail = writeCaseClass[t, N + 1, A](product, labels)

        head ::: tail

      case _: EmptyTuple => Nil
    }
}
