package pureconfig.module.reflect

import pureconfig.{ CamelCase, ConfigFieldMapping, KebabCase }

/**
 * A trait that can be implemented to customize how case classes are read from and written to a config.
 *
 * @tparam T the type of case class for which this hint applies
 */
trait ReflectProductHint[T] {

  /**
   * Returns the key in the config object associated with a given case class field.
   *
   * @param fieldName the case class field
   * @return the key in the config object associated with the given case class field.
   */
  def configKey(fieldName: String): String

}

private[pureconfig] case class ReflectProductHintImpl[T](
    fieldMapping: ConfigFieldMapping) extends ReflectProductHint[T] {

  def configKey(fieldName: String) = fieldMapping(fieldName)
}

object ReflectProductHint {

  def apply[T](
    fieldMapping: ConfigFieldMapping = ConfigFieldMapping(CamelCase, KebabCase)): ReflectProductHint[T] =
    ReflectProductHintImpl[T](fieldMapping)

  implicit def default[T]: ReflectProductHint[T] = apply()
}
