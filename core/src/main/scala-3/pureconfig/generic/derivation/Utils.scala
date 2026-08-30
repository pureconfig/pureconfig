package pureconfig.generic
package derivation

import scala.compiletime.{constValue, erasedValue, summonFrom}
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
    * @param descend
    *   whether to descend to nested sum types and yield leaves
    * @return
    *   the list of transformed labels.
    */
  inline def transformedLabels[A](inline transform: String => String, inline descend: Boolean = false)(using
      m: Mirror.Of[A]
  ): List[String] =
    inline m match {
      case sum: Mirror.SumOf[A] =>
        transformedLabelsTuple[sum.MirroredElemTypes](transform, descend)
      case product: Mirror.ProductOf[A] =>
        transformedLabelsTuple[product.MirroredElemLabels](transform, descend)
    }

  private inline def transformedLabelsTuple[T <: Tuple](
      inline transform: String => String,
      inline descend: Boolean
  ): List[String] =
    inline erasedValue[T] match {
      case _: (h *: t) =>
        val labels = summonFrom {
          case m: Mirror.SumOf[`h`] if descend => transformedLabelsTuple[m.MirroredElemTypes](transform, descend)
          case m: Mirror.Of[`h`] => List(transform(constValue[m.MirroredLabel]))
          case _ => List(transform(constValue[h & String]))
        }
        labels ::: transformedLabelsTuple[t](transform, descend)
      case _: EmptyTuple => Nil
    }

  inline def typeName[A]: String = ${ typeNameImpl[A] }

  private def typeNameImpl[A](using Type[A], Quotes): Expr[String] = Expr(Type.show[A])

}
