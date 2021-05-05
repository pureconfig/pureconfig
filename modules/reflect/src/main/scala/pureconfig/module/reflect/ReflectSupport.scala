package pureconfig.module.reflect

import java.lang.reflect.Modifier

import scala.reflect.{ ClassTag, NameTransformer }
import scala.util.control.NonFatal

private[reflect] object ReflectSupport {
  //Code here is taken from https://github.com/spray/spray-json/blob/release/1.3.x/src/main/scala/spray/json/ProductFormats.scala
  //Reflection is used to discover case class properties and then names are 'unmangeled'

  def extractFieldNames(tag: ClassTag[_]): Array[String] = {
    val clazz = tag.runtimeClass
    try {
      // copy methods have the form copy$default$N(), we need to sort them in order, but must account for the fact
      // that lexical sorting of ...8(), ...9(), ...10() is not correct, so we extract N and sort by N.toInt
      val copyDefaultMethods = clazz.getMethods.filter(_.getName.startsWith("copy$default$")).sortBy(
        _.getName.drop("copy$default$".length).takeWhile(_ != '(').toInt)
      val fields = clazz.getDeclaredFields.filterNot { f =>
        import Modifier._
        (f.getModifiers & (TRANSIENT | STATIC | 0x1000 /* SYNTHETIC*/ )) > 0
      }
      if (copyDefaultMethods.length != fields.length)
        sys.error("Case class " + clazz.getName + " declares additional fields")
      if (fields.zip(copyDefaultMethods).exists { case (f, m) => f.getType != m.getReturnType })
        sys.error("Cannot determine field order of case class " + clazz.getName)
      fields.map(f => unmangle(f.getName))
    } catch {
      case NonFatal(ex) => throw new RuntimeException("Cannot automatically determine case class field names and order " +
        "for '" + clazz.getName + "', please use the 'forProduct' overload with explicit field name specification", ex)
    }
  }

  private def unmangle(name: String) = NameTransformer.decode(name)
}
