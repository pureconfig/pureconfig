package pureconfig.generic
package derivation

import scala.compiletime.{constValue, erasedValue}
import scala.deriving.Mirror
import scala.quoted._

object Utils {

  /** Asserts in compile time that a given value of type `A` is also of type `B`.
    *
    * @param a
    *   the value whose type to assert
    * @return
    *   `a` with a widened type.
    */
  inline def widen[A, B](a: A): A & B =
    inline a match { case b: B => b }

  /** Materializes the labels of a `A` (e.g. product element names, coproduct options) as a list of strings with an
    * optional compile-time transformation. The function is guaranteed to return a constant list.
    *
    * @param transform
    *   the function to transform keys with
    * @return
    *   the list of transformed labels.
    */
  inline def transformedLabels[A](inline transform: String => String)(using m: Mirror.Of[A]): List[String] =
    transformedLabelsTuple[m.MirroredElemLabels](transform)

  private inline def transformedLabelsTuple[T <: Tuple](inline transform: String => String): List[String] =
    inline erasedValue[T] match {
      case _: (h *: t) => transform(constValue[h & String]) :: transformedLabelsTuple[t](transform)
      case _: EmptyTuple => Nil
    }

  object TypeName {
    inline def apply[A]: String = ${ typeNameImpl[A] }

    def typeNameImpl[A](using Type[A], Quotes): Expr[String] = Expr(Type.show[A])
  }

}
