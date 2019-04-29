package pureconfig

import scala.collection.Factory
import scala.collection.mutable

/**
  * A compatibility layer for creating `CanBuildFrom`-like generic methods that work both on Scala 2.13 and pre-2.13
  * versions.
  *
  * @tparam A the type of elements that get added to the builder
  * @tparam C the type of collection that it produces
  */
trait FactoryCompat[-A, +C] {
  def newBuilder(): mutable.Builder[A, C]
}

object FactoryCompat {
  implicit def fromFactory[A, C](implicit factory: Factory[A, C]): FactoryCompat[A, C] = new FactoryCompat[A, C] {
    override def newBuilder() = factory.newBuilder
  }
}
