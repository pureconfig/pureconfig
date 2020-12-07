package pureconfig

import scala.reflect.ClassTag

trait TypeStringCompat[T] {
  def typeName: String
}

object TypeStringCompat {
  // FIXME: ClassTag are weaker cases of TypeTags, which are not available in Scala 3. This is sufficient for our use
  //        cases in the `tests` project but not for the tests in `pureconfig-generic`, since `ClassTag`s don't know
  //        about the argument types of a given type. As such, for example, we don't have different type strings for
  //        HLists of the same size but different types.
  implicit def fromClassTag[T](implicit t: ClassTag[T]): TypeStringCompat[T] = new TypeStringCompat[T] {
    def typeName = t.runtimeClass.getName.toString
  }
}
