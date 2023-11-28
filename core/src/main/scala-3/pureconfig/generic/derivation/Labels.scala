package pureconfig.generic
package derivation

import scala.compiletime.*
import scala.deriving.Mirror

object Labels {
  inline def transformed[T <: Tuple](inline transform: String => String): List[String] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        transform(constValue[h & String]) :: transformed[t](transform)

      case _: EmptyTuple => Nil
    }
}
