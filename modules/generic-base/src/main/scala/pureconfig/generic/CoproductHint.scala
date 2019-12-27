package pureconfig.generic

import com.typesafe.config.{ ConfigObject, ConfigValue, ConfigValueType }
import pureconfig._
import pureconfig.error._
import pureconfig.generic.CoproductHint.{ Attempt, Skip, Use }
import pureconfig.generic.error.{ CoproductHintException, NoValidCoproductChoiceFound, UnexpectedValueForFieldCoproductHint }
import pureconfig.syntax._

/**
 * A trait that can be implemented to disambiguate between the different options of a coproduct or sealed family.
 *
 * @tparam T the type of the coproduct or sealed family for which this hint applies
 */
trait CoproductHint[T] {

  /**
   * Given a `ConfigCursor` for the sealed family, disambiguate and return what should be performed when trying to read
   * a coproduct option named `name`. This method can decide either to:
   *   - use the `ConfigCursor` and disregard other coproduct options ([[CoproductHint.Use]]);
   *   - attempt to use the `ConfigCursor` but try other coproduct options if reading fails ([[CoproductHint.Attempt]]);
   *   - skip the current coproduct option ([[CoproductHint.Skip]]).
   * This method can return a `Left` if the hint fails to produce a valid [[CoproductHint.Action]].
   *
   * @param cur a `ConfigCursor` at the sealed family option
   * @param name the name of the class or coproduct option to try
   * @return a [[ConfigReader.Result]] of [[CoproductHint.Action]] as defined above.
   */
  def from(cur: ConfigCursor, name: String): ConfigReader.Result[CoproductHint.Action]

  /**
   * Returns a non empty list of failures scoped into the context of a `ConfigCursor`, representing the failure to read
   * an option from the config value.
   *
   * @param cur a `ConfigCursor` at the sealed family option
   * @param attempts the list of attempted reads, in order, for each coproduct option
   * @return a non empty list of reader failures.
   */
  def bottom(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReaderFailures

  /**
   * Given the `ConfigValue` for a specific class or coproduct option, encode disambiguation information and return a
   * config for the sealed family or coproduct.
   *
   * @param cv the `ConfigValue` of the class or coproduct option
   * @param name the name of the class or coproduct option
   * @return the config for the sealed family or coproduct wrapped in a `Right`, or a `Left` with the failure if some
   *         error occurred.
   */
  def to(cv: ConfigValue, name: String): ConfigValue
}

/**
 * Hint where the options are disambiguated by a `key = "value"` field inside the config.
 *
 * This hint will cause derived `ConfigConvert` instance to fail to convert configs to objects if the object has a
 * field with the same name as the disambiguation key.
 *
 * By default, the field value written is the class or coproduct option name converted to kebab case. This mapping can
 * be changed by overriding the method `fieldValue` of this class.
 */
class FieldCoproductHint[T](key: String) extends CoproductHint[T] {

  /**
   * Returns the field value for a class or coproduct option name.
   *
   * @param name the name of the class or coproduct option
   * @return the field value associated with the given class or coproduct option name.
   */
  protected def fieldValue(name: String): String = FieldCoproductHint.defaultMapping(name)

  def from(cur: ConfigCursor, name: String): ConfigReader.Result[CoproductHint.Action] = {
    for {
      objCur <- cur.asObjectCursor.right
      valueCur <- objCur.atKey(key).right
      valueStr <- valueCur.asString.right
    } yield if (valueStr == fieldValue(name)) Use(objCur.withoutKey(key)) else Skip
  }

  def bottom(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReaderFailures =
    attempts match {
      case h :: _ => h._2
      case _ =>
        val valueAtKey = for {
          objCur <- cur.asObjectCursor.right
          valueCur <- objCur.atKey(key).right
        } yield valueCur

        valueAtKey.fold(identity, cur => ConfigReaderFailures(cur.failureFor(UnexpectedValueForFieldCoproductHint(cur.value))))
    }

  def to(cv: ConfigValue, name: String): ConfigValue = {
    cv match {
      case co: ConfigObject if co.containsKey(key) =>
        throw new CoproductHintException(CollidingKeys(key, co.get(key)))
      case co: ConfigObject =>
        Map(key -> fieldValue(name)).toConfig.withFallback(co.toConfig)
      case _ =>
        throw new CoproductHintException(WrongType(cv.valueType, Set(ConfigValueType.OBJECT)))
    }
  }
}

object FieldCoproductHint {
  val defaultMapping: String => String = ConfigFieldMapping(PascalCase, KebabCase)
}

/**
 * Hint where all coproduct options are tried in order. `from` will choose the first option able to deserialize
 * the config without errors, while `to` will write the config as is, with no disambiguation information.
 */
class FirstSuccessCoproductHint[T] extends CoproductHint[T] {
  def from(cur: ConfigCursor, name: String): ConfigReader.Result[CoproductHint.Action] =
    Right(Attempt(cur))

  def bottom(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReaderFailures =
    ConfigReaderFailures(cur.failureFor(NoValidCoproductChoiceFound(cur.value)))

  def to(cv: ConfigValue, name: String): ConfigValue =
    cv
}

object CoproductHint {

  /**
   * What should be done when reading a given coproduct option.
   */
  sealed trait Action

  /**
   * An action to only use the provided `ConfigCursor` and not try other coproduct option.
   *
   * @param cur the `ConfigCursor` to use when reading the coproduct option.
   */
  case class Use(cur: ConfigCursor) extends Action

  /**
   * An action to attempt to use the provided `ConfigCursor`, but try other coproduct options if the current one fails
   * to read.
   *
   * @param cur the `ConfigCursor` to use when reading the coproduct option.
   */
  case class Attempt(cur: ConfigCursor) extends Action

  /**
   * A hint to skip the current coproduct option.
   */
  case object Skip extends Action

  implicit def default[T]: CoproductHint[T] = new FieldCoproductHint[T]("type")
}
