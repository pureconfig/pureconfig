package pureconfig
package generic

import scala.compiletime.{erasedValue, error, summonInline}
import scala.deriving.Mirror

import pureconfig.error.{
  CannotConvert,
  ConfigReaderFailures
}
import pureconfig.generic.error.NoValidCoproductOptionFound

export Enum.deriveForEnum

object Enum {
  inline def deriveForEnum[A](inline transformName: String => String)(using m: Mirror.SumOf[A]): EnumConfigReader[A] = {
    val values = summonCases[m.MirroredElemTypes, A]
    new EnumConfigReader[A]:
      def from(cur: ConfigCursor): ConfigReader.Result[A] =
        for {
          value <- cur.asString
          result <-
            ordinal[A](transformName, value) match {
              case Some(ord) => Right(values(ord))
              case None =>
                for {
                  v <- cur.asConfigValue
                  result <- cur.failed(NoValidCoproductOptionFound(v, Seq.empty))
                } yield result
              }
        } yield result
  }

  inline def summonCases[T <: Tuple, A]: List[A] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        inline summonInline[Mirror.Of[h]] match {
          case m: Mirror.Singleton => m.asInstanceOf[A] :: summonCases[t, A]
          case _ => error("Enums cannot include parameterized cases.")
        }

      case _: EmptyTuple => Nil
    }

  inline def ordinal[A](
    inline transformName: String => String, inline value: String
  )(using m: Mirror.SumOf[A]) = {
    val ord = transformedLabelsFor[m.MirroredElemLabels](transformName).indexOf(value)
    Option.when(ord >= 0)(ord)
  }
}
