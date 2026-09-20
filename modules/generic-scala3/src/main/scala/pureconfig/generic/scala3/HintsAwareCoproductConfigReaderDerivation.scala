package pureconfig
package generic
package scala3

import scala.compiletime._
import scala.deriving.Mirror

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.derivation.Utils
import pureconfig.generic.error.InvalidCoproductOption

trait HintsAwareCoproductConfigReaderDerivation { self: HintsAwareConfigReaderDerivation =>
  inline def deriveSumReader[A](using
      cm: Mirror.SumOf[A],
      cph: CoproductHint[A],
      inline df: DerivationFlow
  ): ConfigReader[A] =
    new ConfigReader[A] {
      val labels = Utils.transformedLabels(identity)
      val readers = labels.zip(summonAllConfigReaders[cm.MirroredElemTypes, A]).toMap

      def from(cur: ConfigCursor): ConfigReader.Result[A] =
        summon[CoproductHint[A]]
          .from(cur, labels.sorted)
          .flatMap {
            case CoproductHint.Use(cursor, option) =>
              readers.get(option) match {
                case Some(reader) => reader.from(cursor)
                case None => ConfigReader.Result.fail[A](cursor.failureFor(InvalidCoproductOption(option)))
              }

            case CoproductHint.Attempt(cursor, options, combineF) =>
              val initial: Either[Vector[(String, ConfigReaderFailures)], A] = Left(Vector.empty)
              val res = options.foldLeft(initial) { (curr, option) =>
                curr.left.flatMap { currentFailures =>
                  readers.get(option) match {
                    case Some(reader) => reader.from(cursor).left.map(f => currentFailures :+ (option -> f))
                    case None =>
                      Left(
                        currentFailures :+
                          (option -> ConfigReaderFailures(cursor.failureFor(InvalidCoproductOption(option))))
                      )
                  }

                }
              }

              res.left.map(combineF)
          }
    }

  private inline def summonAllConfigReaders[T <: Tuple, A](using inline df: DerivationFlow): List[ConfigReader[A]] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        (summonConfigReader[h] :: summonAllConfigReaders[t, A]).asInstanceOf[List[ConfigReader[A]]]
      case _: EmptyTuple => Nil
    }
}
