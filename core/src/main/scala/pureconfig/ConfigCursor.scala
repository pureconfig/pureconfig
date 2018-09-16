package pureconfig

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

import com.typesafe.config.ConfigValueType._
import com.typesafe.config._
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
    castOrFail(STRING, Set(NUMBER, BOOLEAN, STRING), { cv => String.valueOf(cv.unwrapped) })

  /**
   * Casts this cursor to a `ConfigListCursor`.
   *
   * @return a `Right` with this cursor as a list cursor if the cast can be done, `Left` with a list of failures
   *         otherwise.
   */
  def asListCursor: Either[ConfigReaderFailures, ConfigListCursor] =
    if (isUndefined) {
      failed(KeyNotFound.forKeys(path, Set()))
    } else {
      val ve = value.valueType() match {
        case ConfigValueType.OBJECT =>
          val ll =
            value.asInstanceOf[ConfigObject].asScala.foldLeft[Either[List[String], List[(Int, ConfigValue)]]](Right(Nil)) {
              case (acc, (str, v)) =>
                (acc, Try(str.toInt)) match {
                  case (Right(a), Success(b)) => Right(a :+ (b -> v))
                  case (Left(keys), Failure(_)) => Left(keys :+ str)
                  case (_, Failure(_)) => Left(List(str))
                  case (Left(keys), _) => Left(keys)
                }
            }

          ll.left.map(CannotConvertObjectToList)
            .right.map(l => ConfigValueFactory.fromIterable(l.sortBy(_._1).map(_._2).asJava))
        case ConfigValueType.LIST =>
          Right(value.asInstanceOf[ConfigList])
        case other =>
          Left(WrongType(other, Set(LIST, OBJECT)))
      }

      ve.left.flatMap(failed)
        .right.map(ConfigListCursor(_, pathElems))
    }

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
    castOrFail(OBJECT, Set(OBJECT), _.asInstanceOf[ConfigObject]).right.map(ConfigObjectCursor(_, pathElems))

  /**
   * Casts this cursor to a map from config keys to cursors.
   *
   * @return a `Right` with the map pointed to by this cursor if the cast can be done, `Left` with a list of failures
   *         otherwise.
   */
  def asMap: Either[ConfigReaderFailures, Map[String, ConfigCursor]] =
    asObjectCursor.right.map(_.map)

  @inline private final def atPathSegment(pathSegment: PathSegment): Either[ConfigReaderFailures, ConfigCursor] = {
    pathSegment match {
      case PathSegment.Key(k) => this.asObjectCursor.right.flatMap(_.atKey(k))
      case PathSegment.Index(i) => this.asListCursor.right.flatMap(_.atIndex(i))
    }
  }

  /**
   * Returns a cursor to the config at the path composed of given path segments.
   *
   * @param pathSegments the path of the config for which a cursor should be returned
   * @return a `Right` with a cursor to the config at `pathSegments` if such a config exists, a `Left` with a list of
   *         failures otherwise.
   */
  final def atPath(pathSegments: PathSegment*): Either[ConfigReaderFailures, ConfigCursor] = {
    pathSegments.foldLeft(Right(this): Either[ConfigReaderFailures, ConfigCursor]) {
      case (soFar, segment) => soFar.right.flatMap(_.atPathSegment(segment))
    }
  }

  /**
   * Casts this cursor as either a `ConfigListCursor` or a `ConfigObjectCursor`.
   *
   * @return a `Right` with this cursor as a list or object cursor if the cast can be done, `Left` with a list of
   *         failures otherwise.
   */
  def asCollectionCursor: Either[ConfigReaderFailures, Either[ConfigListCursor, ConfigObjectCursor]] = {
    if (isUndefined) {
      failed(KeyNotFound.forKeys(path, Set()))
    } else {
      val listAtLeft = asListCursor.right.map(Left.apply)
      lazy val mapAtRight = asObjectCursor.right.map(Right.apply)
      listAtLeft
        .left.flatMap(_ => mapAtRight)
        .left.flatMap(_ => failed(WrongType(value.valueType, Set(LIST, OBJECT))))
    }
  }

  /**
   * Returns a failed `ConfigReader` result resulting from scoping a `FailureReason` into the context of this cursor.
   *
   * This operation is the easiest way to return a failure from a `ConfigReader`.
   *
   * @param reason the reason of the failure
   * @tparam A the returning type of the `ConfigReader`
   * @return a failed `ConfigReader` result built by scoping `reason` into the context of this cursor.
   */
  def failed[A](reason: FailureReason): Either[ConfigReaderFailures, A] =
    Left(ConfigReaderFailures(failureFor(reason)))

  /**
   * Returns a `ConfigReaderFailure` resulting from scoping a `FailureReason` into the context of this cursor.
   *
   * This operation is useful for constructing `ConfigReaderFailures` when there are multiple `FailureReason`s.
   *
   * @param reason the reason of the failure
   * @return a `ConfigReaderFailure` built by scoping `reason` into the context of this cursor.
   */
  def failureFor(reason: FailureReason): ConfigReaderFailure =
    ConvertFailure(reason, this)

  /**
   * Returns a failed `ConfigReader` result resulting from scoping a `Either[FailureReason, A]` into the context of
   * this cursor.
   *
   * This operation is needed when control of the reading process is passed to a place without a `ConfigCursor`
   * instance providing the nexessary context (for example, when `ConfigReader.fromString` is used. In those scenarios,
   * the call should be wrapped in this method in order to turn `FailureReason` instances into `ConfigReaderFailures`.
   *
   * @param result the result of a config reading operation
   * @tparam A the returning type of the `ConfigReader`
   * @return a `ConfigReader` result built by scoping `reason` into the context of this cursor.
   */
  def scopeFailure[A](result: Either[FailureReason, A]): Either[ConfigReaderFailures, A] =
    result.left.map { reason => ConfigReaderFailures(failureFor(reason), Nil) }

  private[this] def castOrFail[A](
    expectedType: ConfigValueType,
    acceptedConfigTypes: Set[ConfigValueType],
    cast: ConfigValue => A): Either[ConfigReaderFailures, A] = {

    if (isUndefined) {
      failed(KeyNotFound.forKeys(path, Set()))
    } else if (!acceptedConfigTypes.contains(value.valueType)) {
      failed(WrongType(value.valueType, Set(expectedType)))
    } else {
      Try(cast(value)) match {
        case Success(v) => Right(v)
        case Failure(ex) =>
          // this should never happen, this is just a safety net
          failed(CannotConvert(value.toString, expectedType.toString, ex.toString))
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
      case idxCur if idxCur.isUndefined => failed(KeyNotFound.forKeys(indexKey(idx), Set()))
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
      case keyCur if keyCur.isUndefined => failed(KeyNotFound.forKeys(key, keys))
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
