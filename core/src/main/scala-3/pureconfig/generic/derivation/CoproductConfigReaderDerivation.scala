package pureconfig
package generic
package derivation

import scala.compiletime.*
import scala.deriving.Mirror
import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.error.InvalidCoproductOption

trait CoproductConfigReaderDerivation:
  self: ConfigReaderDerivation =>

  inline def deriveSumReader[A](using m: Mirror.SumOf[A], ch: CoproductHint[A], ph: ProductHint[A]): ConfigReader[A] =
    new ConfigReader[A]:
      val labels = Labels.transformed[m.MirroredElemLabels](identity)
      val readers = labels.zip(summonAllConfigReaders[m.MirroredElemTypes, A]).toMap

      def from(cur: ConfigCursor): ConfigReader.Result[A] =
        summon[CoproductHint[A]]
          .from(cur, labels.sorted)
          .flatMap:
            case CoproductHint.Use(cursor, option) =>
              readers.get(option) match
                case Some(reader) => reader.from(cursor)
                case None => ConfigReader.Result.fail[A](cursor.failureFor(InvalidCoproductOption(option)))

            case CoproductHint.Attempt(cursor, options, combineF) =>
              val initial: Either[Vector[(String, ConfigReaderFailures)], A] = Left(Vector.empty)
              val res = options.foldLeft(initial): (curr, option) =>
                curr.left.flatMap: currentFailures =>
                  readers.get(option) match
                    case Some(reader) => reader.from(cursor).left.map(f => currentFailures :+ (option -> f))
                    case None =>
                      Left(
                        currentFailures :+
                          (option -> ConfigReaderFailures(cursor.failureFor(InvalidCoproductOption(option))))
                      )

              res.left.map(combineF)

  inline def summonAllConfigReaders[T <: Tuple, A]: List[ConfigReader[A]] =
    inline erasedValue[T] match
      case _: (h *: t) =>
        (summonConfigReader[h] :: summonAllConfigReaders[t, A]).asInstanceOf[List[ConfigReader[A]]]
      case _: EmptyTuple => Nil

  inline def summonConfigReader[A]: ConfigReader[A] =
    summonFrom:
      case reader: ConfigReader[A] => reader
      case m: Mirror.Of[A] =>
        deriveReader[A](using m, summonInline[ProductHint[A]], summonInline[CoproductHint[A]])
