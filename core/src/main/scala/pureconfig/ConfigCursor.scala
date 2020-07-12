package pureconfig

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

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
    * The optional `ConfigValue` which this cursor points to.
    */
  def valueOpt: Option[ConfigValue]

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
  def origin: Option[ConfigOrigin] = valueOpt.map(_.origin())

  /**
    * Returns whether this cursor points to an undefined value. A cursor can point to an undefined value when a missing
    * config key is requested or when a `null` `ConfigValue` is provided, among other reasons.
    *
    * @return `true` if this cursor points to an undefined value, `false` otherwise.
    */
  def isUndefined: Boolean = valueOpt.isEmpty

  /**
    * Returns whether this cursor points to a `null` config value. An explicit `null` value is different than a missing
    * value - `isUndefined` can be used to check for the latter.
    *
    * @return `true` if this cursor points to a `null` value, `false` otherwise.
    */
  def isNull: Boolean = valueOpt.exists(_.unwrapped == null)

  /**
    * Casts this cursor to a `ConfigValue`.
    *
    * @return a `Right` with the config value pointed to by this cursor if the value is defined, `Left` with a list of
    *         failures otherwise.
    */
  def asConfigValue: ConfigReader.Result[ConfigValue] = {
    valueOpt match {
      case Some(value) => Right(value)
      case None => failed(KeyNotFound.forKeys(path, Set()))
    }
  }

  /**
    * Casts this cursor to a string.
    *
    * @return a `Right` with the string value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asString: ConfigReader.Result[String] =
    castOrFail(STRING, v => Right(v.unwrapped.asInstanceOf[String]))

  /**
    * Casts this cursor to a boolean.
    *
    * @return a `Right` with the boolean value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asBoolean: ConfigReader.Result[Boolean] =
    castOrFail(BOOLEAN, v => Right(v.unwrapped.asInstanceOf[Boolean]))

  /**
    * Casts this cursor to a long.
    *
    * @return a `Right` with the long value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asLong: ConfigReader.Result[Long] =
    castOrFail(
      NUMBER,
      _.unwrapped match {
        case i: java.lang.Long => Right(i)
        case i: java.lang.Integer => Right(i.longValue())
        case i: java.lang.Double if i.longValue().toDouble == i => Right(i.longValue())
        case v => Left(CannotConvert(v.toString, "Long", "Unable to convert Number to Long"))
      }
    )

  /**
    * Casts this cursor to an int.
    *
    * @return a `Right` with the int value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asInt: ConfigReader.Result[Int] =
    castOrFail(
      NUMBER,
      _.unwrapped match {
        case i: java.lang.Long if i.intValue().toLong == i => Right(i.intValue())
        case i: java.lang.Integer => Right(i)
        case i: java.lang.Double if i.intValue().toDouble == i => Right(i.intValue())
        case v => Left(CannotConvert(v.toString, "Int", "Unable to convert Number to Int"))
      }
    )

  /**
    * Casts this cursor to a short.
    *
    * @return a `Right` with the short value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asShort: ConfigReader.Result[Short] =
    castOrFail(
      NUMBER,
      _.unwrapped match {
        case i: java.lang.Long if i.shortValue().toLong == i => Right(i.shortValue())
        case i: java.lang.Integer if i.shortValue().toInt == i => Right(i.shortValue())
        case i: java.lang.Double if i.shortValue().toDouble == i => Right(i.shortValue())
        case v => Left(CannotConvert(v.toString, "Short", "Unable to convert Number to Short"))
      }
    )

  /**
    * Casts this cursor to a byte.
    *
    * @return a `Right` with the byte value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asByte: ConfigReader.Result[Byte] =
    castOrFail(
      NUMBER,
      _.unwrapped match {
        case i: java.lang.Long if i.byteValue().toLong == i => Right(i.byteValue())
        case i: java.lang.Integer if i.byteValue().toInt == i => Right(i.byteValue())
        case i: java.lang.Double if i.byteValue().toDouble == i => Right(i.byteValue())
        case v => Left(CannotConvert(v.toString, "Byte", "Unable to convert Number to Byte"))
      }
    )

  /**
    * Casts this cursor to a double.
    *
    * @return a `Right` with the double value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asDouble: ConfigReader.Result[Double] =
    castOrFail(
      NUMBER,
      _.unwrapped match {
        case i: java.lang.Long if i.doubleValue().toLong == i => Right(i.doubleValue())
        case i: java.lang.Integer if i.doubleValue().toInt == i => Right(i.doubleValue())
        case i: java.lang.Double => Right(i)
        case v => Left(CannotConvert(v.toString, "Double", "Unable to convert Number to Double"))
      }
    )

  /**
    * Casts this cursor to a float.
    *
    * @return a `Right` with the float value pointed to by this cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  def asFloat: ConfigReader.Result[Float] =
    castOrFail(
      NUMBER,
      _.unwrapped match {
        case i: java.lang.Long if i.floatValue().toLong == i => Right(i.floatValue())
        case i: java.lang.Integer if i.floatValue().toInt == i => Right(i.floatValue())
        case i: java.lang.Double => Right(i.floatValue())
        case v => Left(CannotConvert(v.toString, "Float", "Unable to convert Number to Float"))
      }
    )

  /**
    * Casts this cursor to a `ConfigListCursor`.
    *
    * @return a `Right` with this cursor as a list cursor if the cast can be done, `Left` with a list of failures
    *         otherwise.
    */
  def asListCursor: ConfigReader.Result[ConfigListCursor] =
    castOrFail(LIST, v => Right(v.asInstanceOf[ConfigList])).right.map(ConfigListCursor(_, pathElems))

  /**
    * Casts this cursor to a list of cursors.
    *
    * @return a `Right` with the list pointed to by this cursor if the cast can be done, `Left` with a list of failures
    *         otherwise.
    */
  def asList: ConfigReader.Result[List[ConfigCursor]] =
    asListCursor.right.map(_.list)

  /**
    * Casts this cursor to a `ConfigObjectCursor`.
    *
    * @return a `Right` with this cursor as an object cursor if it points to an object, `Left` with a list of failures
    *         otherwise.
    */
  def asObjectCursor: ConfigReader.Result[ConfigObjectCursor] =
    castOrFail(OBJECT, v => Right(v.asInstanceOf[ConfigObject])).right.map(ConfigObjectCursor(_, pathElems))

  /**
    * Casts this cursor to a map from config keys to cursors.
    *
    * @return a `Right` with the map pointed to by this cursor if the cast can be done, `Left` with a list of failures
    *         otherwise.
    */
  def asMap: ConfigReader.Result[Map[String, ConfigCursor]] =
    asObjectCursor.right.map(_.map)

  /**
    * Returns a cursor to the config at the path composed of given path segments.
    *
    * @param pathSegments the path of the config for which a cursor should be returned
    * @return a `Right` with a cursor to the config at `pathSegments` if such a config exists, a `Left` with a list of
    *         failures otherwise.
    */
  @deprecated("Use `.fluent.at(pathSegments).cursor` instead", "0.10.2")
  final def atPath(pathSegments: PathSegment*): ConfigReader.Result[ConfigCursor] = fluent.at(pathSegments: _*).cursor

  /**
    * Casts this cursor as either a `ConfigListCursor` or a `ConfigObjectCursor`.
    *
    * @return a `Right` with this cursor as a list or object cursor if the cast can be done, `Left` with a list of
    *         failures otherwise.
    */
  @deprecated("Use `asListCursor` and/or `asObjectCursor` instead", "0.10.1")
  def asCollectionCursor: ConfigReader.Result[Either[ConfigListCursor, ConfigObjectCursor]] = {
    asConfigValue.right.flatMap { value =>
      val listAtLeft = asListCursor.right.map(Left.apply)
      lazy val mapAtRight = asObjectCursor.right.map(Right.apply)
      listAtLeft.left.flatMap(_ => mapAtRight).left.flatMap(_ => failed(WrongType(value.valueType, Set(LIST, OBJECT))))
    }
  }

  def fluent: FluentConfigCursor =
    FluentConfigCursor(asConfigValue.right.map(_ => this))

  /**
    * Returns a failed `ConfigReader` result resulting from scoping a `FailureReason` into the context of this cursor.
    *
    * This operation is the easiest way to return a failure from a `ConfigReader`.
    *
    * @param reason the reason of the failure
    * @tparam A the returning type of the `ConfigReader`
    * @return a failed `ConfigReader` result built by scoping `reason` into the context of this cursor.
    */
  def failed[A](reason: FailureReason): ConfigReader.Result[A] =
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
  def scopeFailure[A](result: Either[FailureReason, A]): ConfigReader.Result[A] =
    result.left.map { reason => ConfigReaderFailures(failureFor(reason)) }

  private[this] def castOrFail[A](
      expectedType: ConfigValueType,
      cast: ConfigValue => Either[FailureReason, A]
  ): ConfigReader.Result[A] = {

    asConfigValue.right.flatMap { value =>
      scopeFailure(ConfigCursor.transform(value, expectedType).right.flatMap(cast))
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
  def apply(value: ConfigValue, pathElems: List[String]): ConfigCursor = SimpleConfigCursor(Option(value), pathElems)

  /**
    * Builds a `ConfigCursor` for an optional `ConfigValue` at a path.
    *
    * @param value the optional `ConfigValue` to which the cursor should point
    * @param pathElems the path of `value` in the config as a list of keys
    * @return a `ConfigCursor` for `value` at the path given by `pathElems`.
    */
  def apply(value: Option[ConfigValue], pathElems: List[String]): ConfigCursor = SimpleConfigCursor(value, pathElems)

  /**
    * Handle automatic type conversions of `ConfigValue`s the way the
    * [[https://github.com/lightbend/config/blob/master/HOCON.md#automatic-type-conversions HOCON specification]]
    * describes. This code mimics the behavior of the package-private `com.typesafe.config.impl.DefaultTransformer` class
    * in Typesafe Config.
    */
  private[pureconfig] def transform(
      configValue: ConfigValue,
      requested: ConfigValueType
  ): Either[WrongType, ConfigValue] = {
    (configValue.valueType(), requested) match {
      case (valueType, requestedType) if valueType == requestedType =>
        Right(configValue)

      case (ConfigValueType.STRING, requestedType) =>
        val s = configValue.unwrapped.asInstanceOf[String]

        requestedType match {
          case ConfigValueType.NUMBER =>
            lazy val tryLong = Try(s.toLong).map(ConfigValueFactory.fromAnyRef)
            lazy val tryDouble = Try(s.toDouble).map(ConfigValueFactory.fromAnyRef)
            // Try#toEither is only available in Scala 2.12+.
            tryLong.orElse(tryDouble) match {
              case Success(value) => Right(value)
              case Failure(_) => Left(WrongType(configValue.valueType, Set(NUMBER)))
            }

          case ConfigValueType.NULL if s == "null" =>
            Right(ConfigValueFactory.fromAnyRef(null))

          case ConfigValueType.BOOLEAN if s == "true" || s == "yes" || s == "on" =>
            Right(ConfigValueFactory.fromAnyRef(true))

          case ConfigValueType.BOOLEAN if s == "false" || s == "no" || s == "off" =>
            Right(ConfigValueFactory.fromAnyRef(false))

          case other =>
            Left(WrongType(configValue.valueType(), Set(other)))
        }

      case (ConfigValueType.NUMBER | ConfigValueType.BOOLEAN, ConfigValueType.STRING) =>
        Right(ConfigValueFactory.fromAnyRef(configValue.unwrapped.toString))

      case (ConfigValueType.OBJECT, ConfigValueType.LIST) =>
        val obj = configValue.asInstanceOf[ConfigObject].asScala.toIterator
        val ll = obj.flatMap { case (str, v) => Try(str.toInt).toOption.map(_ -> v) }.toList

        ll match {
          case l if l.nonEmpty => Right(ConfigValueFactory.fromIterable(l.sortBy(_._1).map(_._2).asJava))
          case _ => Left(WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST)))
        }

      case (valueType, requestedType) =>
        Left(WrongType(valueType, Set(requestedType)))
    }
  }
}

/**
  * A simple `ConfigCursor` providing no extra operations.
  */
case class SimpleConfigCursor(valueOpt: Option[ConfigValue], pathElems: List[String]) extends ConfigCursor

/**
  * A `ConfigCursor` pointing to a config list.
  */
case class ConfigListCursor(listValue: ConfigList, pathElems: List[String], offset: Int = 0) extends ConfigCursor {

  @inline private[this] def validIndex(idx: Int) = idx >= 0 && idx < size
  @inline private[this] def indexKey(idx: Int) = (offset + idx).toString

  def valueOpt: Option[ConfigList] = Some(listValue)

  override def asConfigValue: ConfigReader.Result[ConfigList] = Right(listValue)

  /**
    * Returns whether the config list pointed to by this cursor is empty.
    */
  def isEmpty: Boolean = listValue.isEmpty

  /**
    * Returns the size of the config list pointed to by this cursor.
    */
  def size: Int = listValue.size

  /**
    * Returns a cursor to the config at a given index.
    *
    * @param idx the index of the config for which a cursor should be returned
    * @return a `Right` with a cursor to the config at `idx` if such a config exists, a `Left` with a list of failures
    *         otherwise.
    */
  def atIndex(idx: Int): ConfigReader.Result[ConfigCursor] = {
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
    ConfigCursor(if (validIndex(idx)) Some(listValue.get(idx)) else None, indexKey(idx) :: pathElems)

  /**
    * Returns a cursor to the tail of the config list pointed to by this cursor if non-empty.
    *
    * @return a `Some` with the tail of the config list if the list is not empty, `None` otherwise.
    */
  def tailOption: Option[ConfigListCursor] = {
    if (listValue.isEmpty) None
    else {
      val newValue = ConfigValueFactory
        .fromAnyRef(listValue.asScala.drop(1).asJava)
        .withOrigin(listValue.origin)
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
    listValue.asScala.toList.drop(offset).zipWithIndex.map {
      case (cv, idx) => ConfigCursor(cv, indexKey(idx) :: pathElems)
    }

  // Avoid resetting the offset when using ConfigCursor's implementation.
  override def asListCursor: ConfigReader.Result[ConfigListCursor] = Right(this)
}

/**
  * A `ConfigCursor` pointing to a config object.
  */
case class ConfigObjectCursor(objValue: ConfigObject, pathElems: List[String]) extends ConfigCursor {

  def valueOpt: Option[ConfigObject] = Some(objValue)

  override def asConfigValue: ConfigReader.Result[ConfigObject] = Right(objValue)

  /**
    * Returns whether the config object pointed to by this cursor is empty.
    */
  def isEmpty: Boolean = objValue.isEmpty

  /**
    * Returns the size of the config object pointed to by this cursor.
    */
  def size: Int = objValue.size

  /**
    * Returns the list of keys of the config object pointed to by this cursor.
    */
  def keys: Iterable[String] =
    objValue.keySet.asScala

  /**
    * Returns a cursor to the config at a given key.
    *
    * @param key the key of the config for which a cursor should be returned
    * @return a `Right` with a cursor to the config at `key` if such a config exists, a `Left` with a list of failures
    *         otherwise.
    */
  def atKey(key: String): ConfigReader.Result[ConfigCursor] = {
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
    ConfigCursor(objValue.get(key), key :: pathElems)

  /**
    * Returns a cursor to the object pointed to by this cursor without a given key.
    *
    * @param key the key to remove on the config object
    * @return a cursor to the object pointed to by this cursor without `key`.
    */
  def withoutKey(key: String): ConfigObjectCursor =
    ConfigObjectCursor(objValue.withoutKey(key), pathElems)

  /**
    * Returns a map of cursors to the elements of the config object pointed to by this cursor.
    */
  def map: Map[String, ConfigCursor] =
    objValue.asScala.toMap.map { case (key, cv) => key -> ConfigCursor(cv, key :: pathElems) }

  // Avoid unnecessary cast.
  override def asObjectCursor: ConfigReader.Result[ConfigObjectCursor] = Right(this)
}
