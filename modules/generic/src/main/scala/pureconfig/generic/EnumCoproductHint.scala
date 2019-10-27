package pureconfig.generic

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigValue, ConfigValueType }
import pureconfig._
import pureconfig.error._
import pureconfig.syntax._

/**
 * Hint applicable to sealed families of case objects where objects are written and read as strings with their type
 * names. Trying to read or write values that are not case objects results in failure.
 *
 * @tparam T the type of the coproduct or sealed family for which this hint applies
 */
@deprecated("Use `pureconfig.generic.semiauto.deriveEnumerationReader[T]`, `pureconfig.generic.semiauto.deriveEnumerationWriter[T]` and `pureconfig.generic.semiauto.deriveEnumerationConvert[T]` instead", "0.11.0")
class EnumCoproductHint[T] extends CoproductHint[T] {

  /**
   * Returns the field value for a class or coproduct option name.
   *
   * @param name the name of the class or coproduct option
   * @return the field value associated with the given class or coproduct option name.
   */
  protected def fieldValue(name: String): String = name.toLowerCase

  def from[A <: T](cur: ConfigCursor, reader: ConfigReader[A], name: String, rest: => ConfigReader.Result[T]): ConfigReader.Result[T] =
    cur.asString.right.flatMap { str =>
      if (str == fieldValue(name)) reader.from(ConfigCursor(ConfigFactory.empty.root, cur.pathElems)) else rest
    }

  // TODO: improve handling of failures on the write side
  def to[A <: T](writer: ConfigWriter[A], name: String, value: A): ConfigReader.Result[ConfigValue] =
    writer.to(value) match {
      case co: ConfigObject if co.isEmpty => Right(fieldValue(name).toConfig)
      case cv: ConfigObject => Left(ConfigReaderFailures(ConvertFailure(
        NonEmptyObjectFound(name), ConfigValueLocation(cv), "")))
      case cv => Left(ConfigReaderFailures(ConvertFailure(
        WrongType(cv.valueType, Set(ConfigValueType.OBJECT)), ConfigValueLocation(cv), "")))
    }
}
