package pureconfig

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

/** A compatibility layer for creating `CanBuildFrom`-like generic methods that work both on Scala 2.13 and pre-2.13
  * versions.
  *
  * @tparam A
  *   the type of elements that get added to the builder
  * @tparam C
  *   the type of collection that it produces
  */
trait FactoryCompat[-A, +C] {
  def newBuilder(): mutable.Builder[A, C]
}

object FactoryCompat {
  implicit def fromCanBuildFrom[From, Elem, To](implicit cbf: CanBuildFrom[From, Elem, To]): FactoryCompat[Elem, To] =
    new FactoryCompat[Elem, To] {
      override def newBuilder() = cbf()
    }
}
