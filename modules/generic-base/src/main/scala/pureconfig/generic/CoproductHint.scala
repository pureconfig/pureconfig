package pureconfig.generic

import com.typesafe.config.{ ConfigObject, ConfigValue, ConfigValueType }
import pureconfig._
import pureconfig.error._
import pureconfig.generic.CoproductHint.{ Attempt, Skip, Use }
import pureconfig.generic.error.{ NoValidCoproductChoiceFound, UnexpectedValueForFieldCoproductHint }
import pureconfig.syntax._

/**
 * A trait that can be implemented to disambiguate between the different options of a coproduct or sealed family.
 *
 * @tparam T the type of the coproduct or sealed family for which this hint applies
 */
trait CoproductHint[T] {

  /**
   * Given a [[ConfigCursor]] for the sealed family, disambiguate and return what should be performed when trying to read
   * a coproduct option named `name`. This method can decide either to:
   *   - use the [[ConfigCursor]] and disregard other coproduct options ([[CoproductHint.Use]]);
   *   - attempt to use the [[ConfigCursor]] but try other coproduct options if reading fails ([[CoproductHint.Attempt]];
   *   - skip the current coproduct option ([[CoproductHint.Skip]].
   *
   * @param cur a `ConfigCursor` at the sealed family option
   * @param name the name of the class or coproduct option to try
   * @return a [[CoproductHint.ChoiceHint]] as defined above.
   */
  def from(cur: ConfigCursor, name: String): CoproductHint.ChoiceHint

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
   * @param name the name of the class or coproduct option
   * @param cv the `ConfigValue` of the class or coproduct option
   * @return the config for the sealed family or coproduct wrapped in a `Right`, or a `Left` with the failure if some error
   *         occurred.
   */
  def to(name: String, cv: ConfigValue): ConfigReader.Result[ConfigValue]
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

  def from(cur: ConfigCursor, name: String): CoproductHint.ChoiceHint = {
    (for {
      objCur <- cur.asObjectCursor.right
      valueCur <- objCur.atKey(key).right
      valueStr <- valueCur.asString.right
    } yield if (valueStr == fieldValue(name)) Use(objCur.withoutKey(key)) else Skip).right.getOrElse(Skip)
  }

  override def bottom(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReaderFailures =
    attempts match {
      case h :: _ => h._2
      case _ => (for {
        objCur <- cur.asObjectCursor.right
        valueCur <- objCur.atKey(key).right
        _ <- valueCur.asString.right
      } yield ConfigReaderFailures(valueCur.failureFor(UnexpectedValueForFieldCoproductHint(valueCur.value)))).fold(identity, identity)
    }

  def to(name: String, cv: ConfigValue): ConfigReader.Result[ConfigValue] = {
    cv match {
      case co: ConfigObject =>
        if (co.containsKey(key)) {
          Left(ConfigReaderFailures(ConvertFailure(
            CollidingKeys(key, co.get(key)), ConfigValueLocation(co), "")))
        } else {
          Right(Map(key -> fieldValue(name)).toConfig.withFallback(co.toConfig))
        }
      case _ =>
        Left(ConfigReaderFailures(ConvertFailure(
          WrongType(cv.valueType, Set(ConfigValueType.OBJECT)),
          ConfigValueLocation(cv), "")))
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
  def from(cur: ConfigCursor, name: String): CoproductHint.ChoiceHint =
    Attempt(cur)

  def bottom(cur: ConfigCursor, attempts: List[(String, ConfigReaderFailures)]): ConfigReaderFailures =
    ConfigReaderFailures(cur.failureFor(NoValidCoproductChoiceFound(cur.value)))

  def to(name: String, cv: ConfigValue): ConfigReader.Result[ConfigValue] =
    Right(cv)
}

object CoproductHint {

  /**
   * A hint on what should be done when reading a given coproduct option.
   */
  sealed trait ChoiceHint

  /**
   * A hint to only use the provided [[ConfigCursor]] and not try other coproduct option.
   *
   * @param cur the [[ConfigCursor]] to use when reading the coproduct option.
   */
  case class Use(cur: ConfigCursor) extends ChoiceHint

  /**
   * A hint to attempt to use the provided [[ConfigCursor]], but try other coproduct options if the current one fails to
   * read.
   *
   * @param cur the [[ConfigCursor]] to use when reading the coproduct option.
   */
  case class Attempt(cur: ConfigCursor) extends ChoiceHint

  /**
   * A hint to skip the current coproduct option.
   */
  case object Skip extends ChoiceHint

  implicit def default[T]: CoproductHint[T] = new FieldCoproductHint[T]("type")
}
