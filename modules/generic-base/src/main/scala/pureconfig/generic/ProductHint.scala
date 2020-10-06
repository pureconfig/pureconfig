package pureconfig.generic

import com.typesafe.config.ConfigValue
import pureconfig._
import pureconfig.error.{ConfigReaderFailures, UnknownKey}

/** A trait that can be implemented to customize how case classes are read from and written to a config.
  *
  * @tparam A the type of case class for which this hint applies
  */
trait ProductHint[A] {

  /** Returns what should be used when attempting to read a case class field with name `fieldName` from the provided
    * `ConfigObjectCursor`.
    *
    * @param cursor the cursor from which to read a value
    * @param fieldName the name of the field in `T`
    * @return a [[ProductHint.Action]] object that signals which cursor to use in order to read this field, the name
    *         of the corresponding field in the config object, whether the field should be removed from the object after
    *         read, and whether to use default values for this particular field.
    */
  def from(cursor: ConfigObjectCursor, fieldName: String): ProductHint.Action

  /** Returns optional failures given the provided `ConfigObjectCursor`.
    *
    * @param cursor a `ConfigObjectCursor` at the configuration root from where the product was read
    * @param usedFields a set of all the used fields when reading the product
    * @return an optional non-empty list of failures.
    */
  def bottom(cursor: ConfigObjectCursor, usedFields: Set[String]): Option[ConfigReaderFailures]

  /** Returns an optional key-value pair that should be used for the field with name `fieldName` in the `ConfigObject`
    * representation of `T`.
    *
    * @param value the optional serialized value of the field
    * @param fieldName the name of the field in `T`
    * @return an optional key-value pair to be used in the `ConfigObject` representation of `T`. If `None`, the field is
    *         omitted from the `ConfigObject` representation.
    */
  def to(value: Option[ConfigValue], fieldName: String): Option[(String, ConfigValue)]
}

private[pureconfig] case class ProductHintImpl[A](
    fieldMapping: ConfigFieldMapping,
    useDefaultArgs: Boolean,
    allowUnknownKeys: Boolean
) extends ProductHint[A] {

  def from(cursor: ConfigObjectCursor, fieldName: String): ProductHint.Action = {
    val keyStr = fieldMapping(fieldName)
    val keyCur = cursor.atKeyOrUndefined(keyStr)
    if (useDefaultArgs)
      ProductHint.UseOrDefault(keyCur, keyStr)
    else
      ProductHint.Use(keyCur, keyStr)
  }

  def bottom(cursor: ConfigObjectCursor, usedFields: Set[String]): Option[ConfigReaderFailures] = {
    if (allowUnknownKeys)
      None
    else {
      val unknownKeys = cursor.map.toList.collect {
        case (k, keyCur) if !usedFields.contains(k) =>
          keyCur.failureFor(UnknownKey(k))
      }
      unknownKeys match {
        case h :: t => Some(ConfigReaderFailures(h, t: _*))
        case Nil => None
      }
    }
  }

  def to(value: Option[ConfigValue], fieldName: String): Option[(String, ConfigValue)] =
    value.map(fieldMapping(fieldName) -> _)
}

object ProductHint {

  /** What should be done when attempting to read a given field from a product.
    */
  sealed trait Action {

    /** The `ConfigCursor` to use when trying to read the field.
      */
    def cursor: ConfigCursor

    /** The name of the field in the `ConfigObject` representation of the product.
      */
    def field: String
  }

  /** An action to use the provided `ConfigCursor` when trying to read a given field.
    *
    * @param cursor the `ConfigCursor` to use when trying to read the field
    * @param field the name of the field in the `ConfigObject` representation of the product
    */
  case class Use(cursor: ConfigCursor, field: String) extends Action

  /** An action to either use the provided `ConfigCursor` (if it isn't null or undefined) or fallback to the default
    * value in the product's constructor.
    *
    * @param cursor the `ConfigCursor` to use when trying to read the field
    * @param field the name of the field in the `ConfigObject` representation of the product
    */
  case class UseOrDefault(cursor: ConfigCursor, field: String) extends Action

  def apply[A](
      fieldMapping: ConfigFieldMapping = ConfigFieldMapping(CamelCase, KebabCase),
      useDefaultArgs: Boolean = true,
      allowUnknownKeys: Boolean = true
  ): ProductHint[A] =
    ProductHintImpl[A](fieldMapping, useDefaultArgs, allowUnknownKeys)

  implicit def default[A]: ProductHint[A] = apply()
}
