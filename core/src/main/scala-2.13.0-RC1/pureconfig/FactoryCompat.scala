package pureconfig

import scala.collection.Factory
import scala.collection.mutable

trait FactoryCompat[-A, +C] {
  def newBuilder(): mutable.Builder[A, C]
}

object FactoryCompat {
  implicit def fromFactory[A, C](implicit factory: Factory[A, C]): FactoryCompat[A, C] = new FactoryCompat[A, C] {
    override def newBuilder() = factory.newBuilder
  }
}
