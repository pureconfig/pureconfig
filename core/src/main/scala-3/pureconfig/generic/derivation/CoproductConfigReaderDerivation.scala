package pureconfig
package generic
package derivation

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.CoproductHint
import pureconfig.generic.derivation.ConfigReaderDerivation
import pureconfig.generic.derivation.WidenType.widen
import pureconfig.generic.error.InvalidCoproductOption

trait CoproductConfigReaderDerivation { self: ConfigReaderDerivation =>
  inline def derivedSum[A](using m: Mirror.SumOf[A], ch: CoproductHint[A], ph: ProductHint[A]): ConfigReader[A] =
    new ConfigReader[A] {
      val options = Labels.transformed[m.MirroredElemLabels](identity)
      val readers = options.zip(deriveForSubtypes[m.MirroredElemTypes, A]).toMap

      def from(cur: ConfigCursor): ConfigReader.Result[A] =
        ch.from(cur, options.sorted).flatMap {
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

  inline def deriveForSubtypes[T <: Tuple, A: ProductHint: CoproductHint]: List[ConfigReader[A]] =
    inline erasedValue[T] match {
      case _: (h *: t) => deriveForSubtype[h, A] :: deriveForSubtypes[t, A]
      case _: EmptyTuple => Nil
    }

  inline def deriveForSubtype[A0, A: ProductHint: CoproductHint]: ConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A0] =>
        reader.map(widen[A0, A](_))

      case m: Mirror.Of[A0] =>
        ConfigReader
          .derived[A0](using m, summonInline[ProductHint[A0]], summonInline[CoproductHint[A0]])
          .map(widen[A0, A](_))
    }
}
