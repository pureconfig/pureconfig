package pureconfig
package generic
package derivation

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror

import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.CoproductHint
import pureconfig.generic.derivation.ConfigReaderDerivation
import pureconfig.generic.derivation.WidenType.widen

trait CoproductConfigReaderDerivation(fieldMapping: ConfigFieldMapping, optionField: String) {
  self: ConfigReaderDerivation =>

  inline def derivedSum[A](using m: Mirror.SumOf[A], ch: CoproductHint[A], ph: ProductHint[A]): ConfigReader[A] =
    new ConfigReader[A] {
      def from(cur: ConfigCursor): ConfigReader.Result[A] =
        for {
          objCur <- cur.asObjectCursor
          optCur <- objCur.atKey(optionField)
          option <- optCur.asString
          result <-
            readers.get(option) match {
              case Some(reader) => reader.from(cur)
              case None =>
                Left(
                  ConfigReaderFailures(
                    optCur.failureFor(
                      CannotConvert(option, constValue[m.MirroredLabel], "The value is not a valid option.")
                    )
                  )
                )
            }
        } yield result

      val readers =
        Labels
          .transformed[m.MirroredElemLabels](fieldMapping)
          .zip(deriveForSubtypes[m.MirroredElemTypes, A])
          .toMap
    }

  inline def deriveForSubtypes[T <: Tuple, A]: List[ConfigReader[A]] =
    inline erasedValue[T] match {
      case _: (h *: t) => deriveForSubtype[h, A] :: deriveForSubtypes[t, A]
      case _: EmptyTuple => Nil
    }

  inline def deriveForSubtype[A0, A]: ConfigReader[A] =
    summonFrom {
      case reader: ConfigReader[A0] =>
        reader.map(widen[A0, A](_))

      case given Mirror.Of[A0] =>
        ConfigReader.derived[A0].map(widen[A0, A](_))
    }
}
