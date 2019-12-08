package pureconfig.generic

import com.typesafe.config.ConfigValue
import pureconfig._
import pureconfig.error.{ ConfigReaderFailures, UnknownKey }
import pureconfig.generic.ProductHint.FieldHint

/**
 * A trait that can be implemented to customize how case classes are read from and written to a config.
 *
 * @tparam T the type of case class for which this hint applies
 */
trait ProductHint[T] {

  /**
   * Returns what should be used when attempting to read a case class field with name `fieldName` from the provided
   * `ConfigObjectCursor`.
   *
   * @param cur the cursor from which to read a value
   * @param fieldName the name of the field in `T`
   * @return a [[ProductHint.FieldHint]] object that signals which cursor to use in order to read this field, the name
   *         of the corresponding field in the config object, whether the field should be removed from the object after
   *         read, and whether to use default values for this particular field.
   */
  def from(cur: ConfigObjectCursor, fieldName: String): FieldHint

  /**
   * Returns optional failures if the provided `ConfigObjectCursor` resulted from reading all fields necessary to
   * produce an instance of type `T`.
   *
   * @param cur the `ConfigObjectCursor` that resulted from reading all fields required to produce an instance of type
   *            `T`
   * @return an optional non-empty list of failures.
   */
  def bottom(cur: ConfigObjectCursor): Option[ConfigReaderFailures]

  /**
   * Returns an optional key-value pair that should be used for the field with name `fieldName` in the `ConfigObject`
   * representation of `T`.
   *
   * @param fieldName the name of the field in `T`
   * @param value the optional serialized value of the field
   * @return an optional key-value pair to be used in the `ConfigObject` representation of `T`. If `None`, the field is
   *         omitted from the `ConfigObject` representation.
   */
  def to(fieldName: String, value: Option[ConfigValue]): Option[(String, ConfigValue)]
}

private[pureconfig] case class ProductHintImpl[T](
    fieldMapping: ConfigFieldMapping,
    useDefaultArgs: Boolean,
    allowUnknownKeys: Boolean) extends ProductHint[T] {

  def from(cur: ConfigObjectCursor, fieldName: String): FieldHint = {
    val keyStr = fieldMapping(fieldName)
    val keyCur = cur.atKeyOrUndefined(keyStr)
    FieldHint(keyCur, keyStr, !allowUnknownKeys, useDefaultArgs)
  }

  def bottom(cur: ConfigObjectCursor): Option[ConfigReaderFailures] = {
    if (!allowUnknownKeys && cur.keys.nonEmpty) {
      val keys = cur.map.toList.map { case (k, keyCur) => keyCur.failureFor(UnknownKey(k)) }
      Some(new ConfigReaderFailures(keys.head, keys.tail))
    } else
      None
  }

  def to(fieldName: String, value: Option[ConfigValue]): Option[(String, ConfigValue)] =
    value.map(fieldMapping(fieldName) -> _)
}

object ProductHint {

  /**
   * A hint on what should be used when attempting to read a given field from a product.
   *
   * @param cursor the cursor to use when reading the field
   * @param field the name of the field in the `ConfigObject` representation of the product
   * @param remove whether the field should be removed from the object after read
   * @param useDefault whether to use default values when reading this field if the cursor is undefined
   */
  case class FieldHint(cursor: ConfigCursor, field: String, remove: Boolean, useDefault: Boolean)

  def apply[T](
    fieldMapping: ConfigFieldMapping = ConfigFieldMapping(CamelCase, KebabCase),
    useDefaultArgs: Boolean = true,
    allowUnknownKeys: Boolean = true): ProductHint[T] =
    ProductHintImpl[T](fieldMapping, useDefaultArgs, allowUnknownKeys)

  implicit def default[T]: ProductHint[T] = apply()
}
