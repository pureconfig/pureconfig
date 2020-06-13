package pureconfig.generic

import com.typesafe.config.{ ConfigObject, ConfigValue, ConfigValueType }
import pureconfig._
import pureconfig.error._
import pureconfig.generic.CoproductHint.{ Attempt, Use }
import pureconfig.generic.error.{ CoproductHintException, NoValidCoproductOptionFound, UnexpectedValueForFieldCoproductHint }
import pureconfig.syntax._

/**
 * A trait that can be implemented to disambiguate between the different options of a coproduct or sealed family.
 *
 * @tparam A the type of the coproduct or sealed family for which this hint applies
 */
trait CoproductHint[A] {

  /**
   * Given a `ConfigCursor` for the sealed family, disambiguate and return what should be performed when trying to read
   * one of the provided coproduct options. This method can decide either to:
   *   - use the `ConfigCursor` with a single option ([[CoproductHint.Use]]);
   *   - or attempt different options in a given order ([[CoproductHint.Attempt]]).
   *
   * This method can return a `Left` if the hint fails to produce a valid [[CoproductHint.Action]].
   *
   * @param cursor a `ConfigCursor` at the sealed family option
   * @param options the names of the coproduct options for the given type
   * @return a `ConfigReader.Result` of [[CoproductHint.Action]] as defined above.
   */
  def from(cursor: ConfigCursor, options: Seq[String]): ConfigReader.Result[CoproductHint.Action]

  /**
   * Given the `ConfigValue` for a specific class or coproduct option, encode disambiguation information and return a
   * config for the sealed family or coproduct.
   *
   * @param value the `ConfigValue` of the class or coproduct option
   * @param name the name of the class or coproduct option
   * @return the config for the sealed family or coproduct wrapped in a `Right`, or a `Left` with the failure if some
   *         error occurred.
   */
  def to(value: ConfigValue, name: String): ConfigValue
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
class FieldCoproductHint[A](key: String) extends CoproductHint[A] {

  /**
   * Returns the field value for a class or coproduct option name.
   *
   * @param name the name of the class or coproduct option
   * @return the field value associated with the given class or coproduct option name.
   */
  protected def fieldValue(name: String): String = FieldCoproductHint.defaultMapping(name)

  def from(cursor: ConfigCursor, options: Seq[String]): ConfigReader.Result[CoproductHint.Action] = {
    for {
      objCur <- cursor.asObjectCursor.right
      valueCur <- objCur.atKey(key).right
      valueStr <- valueCur.asString.right
      option <- options.find(valueStr == fieldValue(_))
        .toRight(ConfigReaderFailures(valueCur.failureFor(UnexpectedValueForFieldCoproductHint(valueCur.value)))).right
    } yield Use(objCur.withoutKey(key), option)
  }

  def to(value: ConfigValue, name: String): ConfigValue = {
    value match {
      case co: ConfigObject if co.containsKey(key) =>
        throw CoproductHintException(CollidingKeys(key, co.get(key)))
      case co: ConfigObject =>
        Map(key -> fieldValue(name)).toConfig.withFallback(co.toConfig)
      case _ =>
        throw CoproductHintException(WrongType(value.valueType, Set(ConfigValueType.OBJECT)))
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
class FirstSuccessCoproductHint[A] extends CoproductHint[A] {
  def from(cursor: ConfigCursor, options: Seq[String]): ConfigReader.Result[CoproductHint.Action] =
    Right(Attempt(cursor, options, failures => ConfigReaderFailures(cursor.failureFor(NoValidCoproductOptionFound(cursor.value, failures)))))

  def to(value: ConfigValue, name: String): ConfigValue =
    value
}

object CoproductHint {

  /**
   * What should be done when reading a given coproduct option.
   */
  sealed trait Action {
    /**
     * The `ConfigCursor` to use when trying to read the coproduct option.
     */
    def cursor: ConfigCursor
  }

  /**
   * An action to only use the provided `ConfigCursor` and not try other options.
   *
   * @param cursor the `ConfigCursor` to use when reading the coproduct option
   * @param option the coproduct option to consider when reading from the provider cursor
   */
  case class Use(cursor: ConfigCursor, option: String) extends Action

  /**
   * An action to attempt to use the provided coproduct options, in the specified order, stopping at the first one that
   * reads successfully.
   *
   * @param cursor the `ConfigCursor` to use when reading the coproduct option
   * @param options the coproduct options to attempt reading, in order
   * @param combineFailures the function to combine all failures in case all attempts to read fail
   */
  case class Attempt(cursor: ConfigCursor, options: Seq[String], combineFailures: Seq[(String, ConfigReaderFailures)] => ConfigReaderFailures) extends Action

  implicit def default[A]: CoproductHint[A] = new FieldCoproductHint[A]("type")
}
