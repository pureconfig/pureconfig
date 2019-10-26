package pureconfig.generic

import com.typesafe.config.ConfigValue
import pureconfig._
import pureconfig.error.{ ConfigReaderFailures, KeyNotFound, UnknownKey }

/**
 * A trait that can be implemented to customize how case classes are read from and written to a config.
 *
 * @tparam T the type of case class for which this hint applies
 */
trait ProductHint[T] {

  /**
   * Reads a field of type `A` from the provided `ConfigObjectCursor` given a `ConfigReader` that is in scope for `A`.
   * This method returns both the result of attempting to read an `A` from the provided cursor as well as the new cursor
   * that should be used to read subsequent fields.
   *
   * @param cur the cursor from which to read a value
   * @param reader the `ConfigReader` currently in scope for `A`
   * @param fieldName the name of the field of type `A` in `T`
   * @param default the default value for the field with name `fieldName` in `T`
   * @tparam A the type of the field to be read
   * @return a tuple with both the result of reading the field and the cursor to supply to subsequent reads.
   */
  def from[A](cur: ConfigObjectCursor, reader: ConfigReader[A], fieldName: String, default: Option[A]): (ConfigReader.Result[A], ConfigObjectCursor)

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
   * Produces an optional key-value pair that should be used for the field with name `fieldName` in its `ConfigObject`
   * representation.
   *
   * @param writer the `ConfigWriter` currently in scope for `A`
   * @param fieldName the name of the field of type `A` in `T`
   * @param value the current value of `fieldName`
   * @tparam A the type of the field to be written
   * @return an optional key-value pair to be used in the `ConfigObject` representation of `T`. If `None`, the field is
   *         omitted from the `ConfigObject` representation.
   */
  def to[A](writer: ConfigWriter[A], fieldName: String, value: A): Option[(String, ConfigValue)]
}

private[pureconfig] case class ProductHintImpl[T](
    fieldMapping: ConfigFieldMapping,
    useDefaultArgs: Boolean,
    allowUnknownKeys: Boolean) extends ProductHint[T] {

  def from[A](cur: ConfigObjectCursor, reader: ConfigReader[A], fieldName: String, default: Option[A]): (ConfigReader.Result[A], ConfigObjectCursor) = {
    val keyStr = fieldMapping(fieldName)
    val result = cur.atKeyOrUndefined(keyStr) match {
      case keyCur if keyCur.isUndefined =>
        default match {
          case Some(defaultValue) if useDefaultArgs => Right(defaultValue)
          case _ if reader.isInstanceOf[ReadsMissingKeys] => reader.from(keyCur)
          case _ => cur.failed(KeyNotFound.forKeys(keyStr, cur.keys))
        }
      case keyCur => reader.from(keyCur)
    }
    val nextCur = if (allowUnknownKeys) cur else cur.withoutKey(keyStr)
    (result, nextCur)
  }

  def bottom(cur: ConfigObjectCursor): Option[ConfigReaderFailures] = {
    if (!allowUnknownKeys && cur.keys.nonEmpty) {
      val keys = cur.map.toList.map { case (k, keyCur) => keyCur.failureFor(UnknownKey(k)) }
      Some(new ConfigReaderFailures(keys.head, keys.tail))
    } else
      None
  }

  def to[A](writer: ConfigWriter[A], fieldName: String, value: A): Option[(String, ConfigValue)] = {
    val keyStr = fieldMapping(fieldName)
    writer match {
      case w: WritesMissingKeys[A @unchecked] =>
        w.toOpt(value).map(keyStr -> _)
      case w =>
        Some((keyStr, w.to(value)))
    }
  }
}

object ProductHint {

  def apply[T](
    fieldMapping: ConfigFieldMapping = ConfigFieldMapping(CamelCase, KebabCase),
    useDefaultArgs: Boolean = true,
    allowUnknownKeys: Boolean = true): ProductHint[T] =
    ProductHintImpl[T](fieldMapping, useDefaultArgs, allowUnknownKeys)

  implicit def default[T]: ProductHint[T] = apply()
}
