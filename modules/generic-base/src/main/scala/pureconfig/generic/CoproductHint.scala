package pureconfig.generic

import com.typesafe.config.{ ConfigObject, ConfigValue, ConfigValueType }
import pureconfig._
import pureconfig.error._
import pureconfig.generic.error.{ NoValidCoproductChoiceFound, UnexpectedValueForFieldCoproductHint }
import pureconfig.syntax._

/**
 * A trait that can be implemented to disambiguate between the different options of a coproduct or sealed family.
 *
 * @tparam T the type of the coproduct or sealed family for which this hint applies
 */
trait CoproductHint[T] {

  /**
   * Given a `ConfigCursor` for the sealed family, disambiguate and return the result of either reading the cursor as an
   * `A` (for which the `ConfigReader` in scope is provided), or defer to a different reader, whose result of reading a
   * `T` is (lazily) provided.
   *
   * @param cur a `ConfigCursor` at the sealed family option
   * @param reader the `ConfigReader` in scope for type `A`
   * @param name the name of the class or coproduct option to try
   * @param rest the result of deferring the reading of `T` as a different coproduct option
   * @tparam A the type of the current class or coproduct option being considered
   * @return the result of attempting to read the provided cursor as a `T`.
   */
  def from[A <: T](cur: ConfigCursor, reader: ConfigReader[A], name: String, rest: => ConfigReader.Result[T]): ConfigReader.Result[T]

  /**
   * Returns a non empty list of failures scoped into the context of a `ConfigCursor`, representing the failure to read
   * an option from the config value.
   *
   * @param cur a `ConfigCursor` at the sealed family option
   * @return a non empty list of reader failures.
   */
  def noOptionFound(cur: ConfigCursor): ConfigReaderFailures =
    ConfigReaderFailures(cur.failureFor(NoValidCoproductChoiceFound(cur.value)))

  /**
   * Given the current value of the coproduct option and the writer currently in scope for it, return the `ConfigValue`
   * with its representation, possibly encoded with disambiguation information.
   *
   * @param writer the `ConfigWriter` in scope for type `A`
   * @param name the name of the class or coproduct option
   * @param value the value of the coproduct option
   * @tparam A the type of the class or coproduct option
   * @return the config for the sealed family or coproduct wrapped in a `Right`, or a `Left` with the failure if some
   *         error occurred.
   */
  def to[A <: T](writer: ConfigWriter[A], name: String, value: A): ConfigReader.Result[ConfigValue]
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

  def from[A <: T](cur: ConfigCursor, reader: ConfigReader[A], name: String, rest: => ConfigReader.Result[T]): ConfigReader.Result[T] =
    for {
      objCur <- cur.asObjectCursor.right
      valueCur <- objCur.atKey(key).right
      valueStr <- valueCur.asString.right
      cur = if (valueStr == fieldValue(name)) Some(objCur.withoutKey(key)) else None
      result <- cur.fold(rest)(value => reader.from(value).right.map(identity[T]))
    } yield result

  override def noOptionFound(cur: ConfigCursor): ConfigReaderFailures =
    cur.fluent.at(key).cursor.fold(
      identity,
      cur => ConfigReaderFailures(cur.failureFor(UnexpectedValueForFieldCoproductHint(cur.value))))

  // TODO: improve handling of failures on the write side
  def to[A <: T](writer: ConfigWriter[A], name: String, value: A): ConfigReader.Result[ConfigValue] = {
    writer.to(value) match {
      case co: ConfigObject =>
        if (co.containsKey(key)) {
          Left(ConfigReaderFailures(ConvertFailure(CollidingKeys(key, co.get(key)), ConfigValueLocation(co), "")))
        } else {
          Right(Map(key -> fieldValue(name)).toConfig.withFallback(co.toConfig))
        }
      case cv =>
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
  def from[A <: T](cur: ConfigCursor, reader: ConfigReader[A], name: String, rest: => ConfigReader.Result[T]): ConfigReader.Result[T] =
    reader.from(cur).right.map(identity[T]).left.flatMap(_ => rest)

  def to[A <: T](writer: ConfigWriter[A], name: String, value: A) =
    Right(writer.to(value))
}

object CoproductHint {
  implicit def default[T]: CoproductHint[T] = new FieldCoproductHint[T]("type")
}
