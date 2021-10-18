package pureconfig

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait TypeStringCompat[T] {
  def typeName: String
}

object TypeStringCompat extends TypeStringCompatLowerPriority {
  implicit def fromTypeTag[T](implicit t: TypeTag[T]): TypeStringCompat[T] = new TypeStringCompat[T] {
    def typeName = t.tpe.toString
  }
}

trait TypeStringCompatLowerPriority {
  // A fallback for types that don't have a TypeTag. ClassTags are weaker cases of TypeTags, not knowing about the
  // argument types of a given type. They therefore produce type strings with less information.
  implicit def fromClassTag[T](implicit t: ClassTag[T]): TypeStringCompat[T] = new TypeStringCompat[T] {
    def typeName = t.runtimeClass.getName.toString
  }
}
