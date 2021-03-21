package pureconfig.generic

import scala.compiletime.{constValue, erasedValue, summonFrom, summonInline}
import scala.deriving.Mirror

inline def labelsFor[T <: Tuple]: List[String] =
  transformedLabelsFor[T](identity)

inline def transformedLabelsFor[T <: Tuple](
  inline transform: String => String
): List[String] =
  inline erasedValue[T] match
    case _: (h *: t) =>
      transform(constValue[h & String]) :: transformedLabelsFor[t](transform)

    case _: EmptyTuple => Nil
