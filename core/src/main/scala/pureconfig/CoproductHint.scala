package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigValue, ConfigValueType }
import pureconfig.error._
import pureconfig.syntax._

/**
 * A trait that can be implemented to disambiguate between the different options of a coproduct or sealed family.
 *
 * @tparam T the type of the coproduct or sealed family for which this hint applies
 */
trait CoproductHint[T] {

  /**
   * Given a `ConfigValue` for the sealed family, disambiguate and extract the `ConfigValue` associated to the
   * implementation for the given class or coproduct option name.
   *
   * If `cv` is a config for the given class name, this method returns `Right(Some(v))`, where `v` is the config
   * related to the specific class (possibly the same as `cv`). If it determines that `cv` is a config for a different
   * class, it returns `Right(None)`. If `cv` is missing information for disambiguation or has a wrong type, a
   * `Left` containing a `Failure` is returned.
   *
   * @param cur a `ConfigCursor` at the sealed family option
   * @param name the name of the class or coproduct option to try
   * @return a `Either[ConfigReaderFailure, Option[ConfigValue]]` as defined above.
   */
  def from(cur: ConfigCursor, name: String): Either[ConfigReaderFailures, Option[ConfigCursor]]

  /**
   * Given the `ConfigValue` for a specific class or coproduct option, encode disambiguation information and return a
   * config for the sealed family or coproduct.
   *
   * @param cv the `ConfigValue` of the class or coproduct option
   * @param name the name of the class or coproduct option
   * @return the config for the sealed family or coproduct wrapped in a `Right`, or a `Left` with the failure if some error
   *         occurred.
   */
  def to(cv: ConfigValue, name: String): Either[ConfigReaderFailures, ConfigValue]

  /**
   * Defines what to do if `from` returns `Success(Some(_))` for a class or coproduct option, but its `ConfigConvert`
   * fails to deserialize the config.
   *
   * @param name the name of the class or coproduct option
   * @return `true` if the next class or coproduct option should be tried, `false` otherwise.
   */
  def tryNextOnFail(name: String): Boolean
}

/**
 * Hint where the options are disambiguated by a `key = "value"` field inside the config.
 *
 * This hint will cause derived `ConfigConvert` instance to fail to convert configs to objects if the object has a
 * field with the same name as the disambiguation key.
 *
 * By default, the field value written is the class or coproduct option name converted to lower case. This mapping can
 * be changed by overriding the method `fieldValue` of this class.
 */
class FieldCoproductHint[T](key: String) extends CoproductHint[T] {

  /**
   * Returns the field value for a class or coproduct option name.
   *
   * @param name the name of the class or coproduct option
   * @return the field value associated with the given class or coproduct option name.
   */
  protected def fieldValue(name: String): String = name.toLowerCase

  def from(cur: ConfigCursor, name: String): Either[ConfigReaderFailures, Option[ConfigCursor]] = {
    for {
      objCur <- cur.asObjectCursor.right
      valueCur <- objCur.atKey(key).right
      valueStr <- valueCur.asString.right
    } yield if (valueStr == fieldValue(name)) Some(objCur.withoutKey(key)) else None
  }

  def to(cv: ConfigValue, name: String): Either[ConfigReaderFailures, ConfigValue] = cv match {
    case co: ConfigObject =>
      if (co.containsKey(key)) Left(ConfigReaderFailures(CollidingKeys(key, co.get(key), ConfigValueLocation(co))))
      else Right(Map(key -> fieldValue(name)).toConfig.withFallback(co.toConfig))

    case _ =>
      Left(ConfigReaderFailures(WrongType(cv.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(cv), "")))
  }

  def tryNextOnFail(name: String) = false
}

/**
 * Hint applicable to sealed families of case objects where objects are written and read as strings with their type
 * names. Trying to read or write values that are not case objects results in failure.
 *
 * @tparam T the type of the coproduct or sealed family for which this hint applies
 */
class EnumCoproductHint[T] extends CoproductHint[T] {

  /**
   * Returns the field value for a class or coproduct option name.
   *
   * @param name the name of the class or coproduct option
   * @return the field value associated with the given class or coproduct option name.
   */
  protected def fieldValue(name: String): String = name.toLowerCase

  def from(cur: ConfigCursor, name: String) = cur.asString.right.map { str =>
    if (str == fieldValue(name)) Some(ConfigCursor(ConfigFactory.empty.root, cur.pathElems)) else None
  }

  def to(cv: ConfigValue, name: String) = cv match {
    case co: ConfigObject if co.isEmpty => Right(fieldValue(name).toConfig)
    case _: ConfigObject => Left(ConfigReaderFailures(NonEmptyObjectFound(name, ConfigValueLocation(cv), "")))
    case _ => Left(ConfigReaderFailures(WrongType(cv.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(cv), "")))
  }

  def tryNextOnFail(name: String) = false
}

/**
 * Hint where all coproduct options are tried in order. `from` will choose the first option able to deserialize
 * the config without errors, while `to` will write the config as is, with no disambiguation information.
 */
class FirstSuccessCoproductHint[T] extends CoproductHint[T] {
  def from(cur: ConfigCursor, name: String) = Right(Some(cur))
  def to(cv: ConfigValue, name: String) = Right(cv)
  def tryNextOnFail(name: String) = true
}

object CoproductHint {
  implicit def default[T]: CoproductHint[T] = new FieldCoproductHint[T]("type")
}
