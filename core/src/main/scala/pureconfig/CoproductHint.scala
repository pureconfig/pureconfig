package pureconfig

import scala.util.{Failure, Success, Try}

import com.typesafe.config.{ConfigObject, ConfigValue}
import pureconfig.error.{CollidingKeysException, KeyNotFoundException, WrongTypeException}
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
   * If `cv` is a config for the given class name, this method returns `Success(Some(v))`, where `v` is the config
   * related to the specific class (possibly the same as `cv`). If it determines that `cv` is a config for a different
   * class, it returns `Success(None)`. If `cv` is missing information for disambiguation or has a wrong type, a
   * `Failure` is returned.
   *
   * @param cv the `ConfigValue` of the sealed family
   * @param name the name of the class or coproduct option to try
   * @return a `Try[Option[ConfigValue]]` as defined above.
   */
  def from(cv: ConfigValue, name: String): Try[Option[ConfigValue]]

  /**
   * Given the `ConfigValue` for a specific class or coproduct option, encode disambiguation information and return a
   * config for the sealed family or coproduct.
   *
   * @param cv the `ConfigValue` of the class or coproduct option
   * @param name the name of the class or coproduct option
   * @return the config for the sealed family or coproduct wrapped in a `Success`, or a `Failure` if some error
   *         occurred.
   */
  def to(cv: ConfigValue, name: String): Try[ConfigValue]

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
   * Returns the field name for a class or coproduct option name.
   *
   * @param name the name of the class or coproduct option
   * @return the field value associated with the given class or coproduct option name.
   */
  protected def fieldValue(name: String): String = name.toLowerCase

  def from(cv: ConfigValue, name: String): Try[Option[ConfigValue]] = cv match {
    case co: ConfigObject =>
      Option(co.get(key)) match {
        case Some(fv) => fv.unwrapped match {
          case v: String if v == fieldValue(name) => Success(Some(cv))
          case _: String => Success(None)
          case _ => Failure(WrongTypeException(fv.valueType.toString))
        }
        case None => Failure(KeyNotFoundException(key))
      }
    case _ => Failure(WrongTypeException(cv.valueType.toString))
  }

  def to(cv: ConfigValue, name: String): Try[ConfigValue] = cv match {
    case co: ConfigObject =>
      if (co.containsKey(key)) Failure(CollidingKeysException(key, co.get(key).toString))
      else Success(Map(key -> fieldValue(name)).toConfig.withFallback(co.toConfig))

    case _ =>
      Failure(WrongTypeException(cv.valueType.toString))
  }

  def tryNextOnFail(name: String) = false
}

/**
 * Hint where all coproduct options are tried in order. `from` will choose the first option able to deserialize
 * the config without errors, while `to` will write the config as is, with no disambiguation information.
 */
class FirstSuccessCoproductHint[T] extends CoproductHint[T] {
  def from(cv: ConfigValue, name: String) = Success(Some(cv))
  def to(cv: ConfigValue, name: String) = Success(cv)
  def tryNextOnFail(name: String) = true
}

object CoproductHint {
  implicit def default[T]: CoproductHint[T] = new FieldCoproductHint[T]("type")
}
