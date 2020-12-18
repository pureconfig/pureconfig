package pureconfig

import scala.reflect.runtime.universe._

trait TypeStringCompat[T] {
  def typeName: String
}

object TypeStringCompat {
  implicit def fromTypeTag[T](implicit t: TypeTag[T]): TypeStringCompat[T] = new TypeStringCompat[T] {
    def typeName = t.tpe.toString
  }
}
