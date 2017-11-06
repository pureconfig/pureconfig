package pureconfig

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

import com.typesafe.config._
import pureconfig.ConvertHelpers._
import pureconfig.backend.PathUtil
import pureconfig.error._

/**
 * A wrapper for a `ConfigValue` providing safe navigation through the config and holding positional data for better
 * error handling.
 */
sealed trait ConfigCursor {

  /**
   * The `ConfigValue` to which this cursor points to.
   */
  def value: ConfigValue

  /**
   * The path in the config to which this cursor points as a list of keys in reverse order (deepest key first).
   */
  def pathElems: List[String]

  /**
   * The path in the config to which this cursor points.
   */
  def path: String = PathUtil.joinPath(pathElems.reverse)

  /**
   * The file system location of the config to which this cursor points.
   */
  def location: Option[ConfigValueLocation] = ConfigValueLocation(value)

  /**
   * Returns whether this cursor points to an undefined value. A cursor can point to an undefined value when a missing
   * config key is requested or when a `null` `ConfigValue` is provided, among other reasons.
   *
   * @return `true` if this cursor points to an undefined value, `false` otherwise.
   */
  def isUndefined: Boolean = value == null

  /**
   * Returns whether this cursor points to a `null` config value. An explicit `null` value is different than a missing
   * value - `isUndefined` can be used to check for the latter.
   *
   * @return `true` if this cursor points to a `null` value, `false` otherwise.
   */
  def isNull: Boolean = value != null && value.unwrapped == null

  /**
   * Casts this cursor to a string.
   *
   * @return a `Right` with the string value pointed to by this cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asString: Either[ConfigReaderFailures, String] =
    castOrFail(ConfigValueType.STRING, _.unwrapped.asInstanceOf[String])

  /**
   * Casts this cursor to a `ConfigListCursor`.
   *
   * @return a `Right` with this cursor as a list cursor if the cast can be done, `Left` with a list of failures
   *         otherwise.
   */
  def asListCursor: Either[ConfigReaderFailures, ConfigListCursor] =
    castOrFail(ConfigValueType.LIST, _.asInstanceOf[ConfigList]).right.map(ConfigListCursor(_, pathElems))

  /**
   * Casts this cursor to a list of cursors.
   *
   * @return a `Right` with the list pointed to by this cursor if the cast can be done, `Left` with a list of failures
   *         otherwise.
   */
  def asList: Either[ConfigReaderFailures, List[ConfigCursor]] =
    asListCursor.right.map(_.list)

  /**
   * Casts this cursor to a `ConfigObjectCursor`.
   *
   * @return a `Right` with this cursor as an object cursor if it points to an object, `Left` with a list of failures
   *         otherwise.
   */
  def asObjectCursor: Either[ConfigReaderFailures, ConfigObjectCursor] =
    castOrFail(ConfigValueType.OBJECT, _.asInstanceOf[ConfigObject]).right.map(ConfigObjectCursor(_, pathElems))

  /**
   * Casts this cursor to a map from config keys to cursors.
   *
   * @return a `Right` with the map pointed to by this cursor if the cast can be done, `Left` with a list of failures
   *         otherwise.
   */
  def asMap: Either[ConfigReaderFailures, Map[String, ConfigCursor]] =
    asObjectCursor.right.map(_.map)

  /**
   * Casts this cursor as either a `ConfigListCursor` or a `ConfigObjectCursor`.
   *
   * @return a `Right` with this cursor as a list or object cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asCollectionCursor: Either[ConfigReaderFailures, Either[ConfigListCursor, ConfigObjectCursor]] = {
    if (isUndefined) {
      fail(KeyNotFound(path, location, Set()))
    } else {
      val listAtLeft = asListCursor.right.map(Left.apply)
      lazy val mapAtRight = asObjectCursor.right.map(Right.apply)
      listAtLeft
        .left.flatMap(_ => mapAtRight)
        .left.flatMap(_ => fail(WrongType(value.valueType, Set(ConfigValueType.LIST, ConfigValueType.OBJECT), location, path)))
    }
  }

  private[this] def castOrFail[A](expectedType: ConfigValueType, cast: ConfigValue => A): Either[ConfigReaderFailures, A] = {
    if (isUndefined) {
      fail(KeyNotFound(path, location, Set()))
    } else if (value.valueType != expectedType) {
      fail(WrongType(value.valueType, Set(expectedType), location, path))
    } else {
      Try(cast(value)) match {
        case Success(v) => Right(v)
        case Failure(ex) =>
          // this should never happen, this is just a safety net
          fail(CannotConvert(value.toString, expectedType.toString, ex.toString, location, path))
      }
    }
  }
}

object ConfigCursor {

  /**
   * Builds a `ConfigCursor` for a `ConfigValue` at a path.
   *
   * @param value the `ConfigValue` to which the cursor should point
   * @param pathElems the path of `value` in the config as a list of keys
   * @return a `ConfigCursor` for `value` at the path given by `pathElems`.
   */
  def apply(value: ConfigValue, pathElems: List[String]): ConfigCursor = SimpleConfigCursor(value, pathElems)
}

/**
 * A simple `ConfigCursor` providing no extra operations.
 */
case class SimpleConfigCursor(value: ConfigValue, pathElems: List[String]) extends ConfigCursor

/**
 * A `ConfigCursor` pointing to a config list.
 */
case class ConfigListCursor(value: ConfigList, pathElems: List[String], offset: Int = 0) extends ConfigCursor {

  @inline private[this] def validIndex(idx: Int) = idx >= 0 && idx < size
  @inline private[this] def indexKey(idx: Int) = (offset + idx).toString

  /**
   * Returns whether the config list pointed to by this cursor is empty.
   */
  def isEmpty: Boolean = value.isEmpty

  /**
   * Returns the size of the config list pointed to by this cursor.
   */
  def size: Int = value.size

  /**
   * Returns a cursor to the config at a given index.
   *
   * @param idx the index of the config for which a cursor should be returned
   * @return a `Right` with a cursor to the config at `idx` if such a config exists, a `Left` with a list of failures
   *         otherwise.
   */
  def atIndex(idx: Int): Either[ConfigReaderFailures, ConfigCursor] = {
    atIndexOrUndefined(idx) match {
      case idxCur if idxCur.isUndefined => fail(KeyNotFound(idxCur.path, location, indexKey(idx), Set()))
      case idxCur => Right(idxCur)
    }
  }

  /**
   * Returns a cursor to the config at a given index. An out of range index will return a cursor to an undefined value.
   *
   * @param idx the index of the config for which a cursor should be returned
   * @return a cursor to the config at `idx` if such a config exists, a cursor to an undefined value otherwise.
   */
  def atIndexOrUndefined(idx: Int): ConfigCursor =
    ConfigCursor(if (validIndex(idx)) value.get(idx) else null, indexKey(idx) :: pathElems)

  /**
   * Returns a cursor to the tail of the config list pointed to by this cursor if non-empty.
   *
   * @return a `Some` with the tail of the config list if the list is not empty, `None` otherwise.
   */
  def tailOption: Option[ConfigListCursor] = {
    if (value.isEmpty) None
    else {
      val newValue = ConfigValueFactory.fromAnyRef(value.asScala.drop(1).asJava)
        .withOrigin(value.origin)
        .asInstanceOf[ConfigList]

      Some(ConfigListCursor(newValue, pathElems, offset = offset + 1))
    }
  }

  /**
   * Returns a list of cursors to the elements of the config list pointed to by this cursor.
   *
   * @return a list of cursors to the elements of the config list pointed to by this cursor.
   */
  def list: List[ConfigCursor] =
    value.asScala.toList.drop(offset).zipWithIndex.map { case (cv, idx) => ConfigCursor(cv, indexKey(idx) :: pathElems) }
}

/**
 * A `ConfigCursor` pointing to a config object.
 */
case class ConfigObjectCursor(value: ConfigObject, pathElems: List[String]) extends ConfigCursor {

  /**
   * Returns whether the config object pointed to by this cursor is empty.
   */
  def isEmpty: Boolean = value.isEmpty

  /**
   * Returns the size of the config object pointed to by this cursor.
   */
  def size: Int = value.size

  /**
   * Returns the list of keys of the config object pointed to by this cursor.
   */
  def keys: Iterable[String] =
    value.keySet.asScala

  /**
   * Returns a cursor to the config at a given key.
   *
   * @param key the key of the config for which a cursor should be returned
   * @return a `Right` with a cursor to the config at `key` if such a config exists, a `Left` with a list of failures
   *         otherwise.
   */
  def atKey(key: String): Either[ConfigReaderFailures, ConfigCursor] = {
    atKeyOrUndefined(key) match {
      case keyCur if keyCur.isUndefined => fail(KeyNotFound(keyCur.path, location, key, keys))
      case keyCur => Right(keyCur)
    }
  }

  /**
   * Returns a cursor to the config at a given key. A missing key will return a cursor to an undefined value.
   *
   * @param key the key of the config for which a cursor should be returned
   * @return a cursor to the config at `key` if such a config exists, a cursor to an undefined value otherwise.
   */
  def atKeyOrUndefined(key: String): ConfigCursor =
    ConfigCursor(value.get(key), key :: pathElems)

  /**
   * Returns a cursor to the object pointed to by this cursor without a given key.
   *
   * @param key the key to remove on the config object
   * @return a cursor to the object pointed to by this cursor without `key`.
   */
  def withoutKey(key: String): ConfigObjectCursor =
    ConfigObjectCursor(value.withoutKey(key), pathElems)

  /**
   * Returns a map of cursors to the elements of the config object pointed to by this cursor.
   */
  def map: Map[String, ConfigCursor] =
    value.asScala.toMap.map { case (key, cv) => key -> ConfigCursor(cv, key :: pathElems) }
}
