package pureconfig

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

trait FactoryCompat[-A, +C] {
  def newBuilder(): mutable.Builder[A, C]
}

object FactoryCompat {
  implicit def fromCanBuildFrom[From, Elem, To](implicit cbf: CanBuildFrom[From, Elem, To]): FactoryCompat[Elem, To] = new FactoryCompat[Elem, To] {
    override def newBuilder() = cbf()
  }
}
