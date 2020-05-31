package pureconfig.generic

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigValue, ConfigValueType }
import pureconfig._
import pureconfig.error._
import pureconfig.syntax._

/**
 * Hint applicable to sealed families of case objects where objects are written and read as strings with their type
 * names. Trying to read or write values that are not case objects results in failure.
 *
 * @tparam A the type of the coproduct or sealed family for which this hint applies
 */
@deprecated("Use `pureconfig.generic.semiauto.deriveEnumerationReader[A]`, `pureconfig.generic.semiauto.deriveEnumerationWriter[A]` and `pureconfig.generic.semiauto.deriveEnumerationConvert[A]` instead", "0.11.0")
class EnumCoproductHint[A] extends CoproductHint[A] {

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

  // TODO: improve handling of failures on the write side
  def to(cv: ConfigValue, name: String) = cv match {
    case co: ConfigObject if co.isEmpty => Right(fieldValue(name).toConfig)
    case _: ConfigObject => Left(ConfigReaderFailures(ConvertFailure(
      NonEmptyObjectFound(name), Some(cv.origin()), "")))
    case _ => Left(ConfigReaderFailures(ConvertFailure(
      WrongType(cv.valueType, Set(ConfigValueType.OBJECT)), Some(cv.origin), "")))
  }

  def tryNextOnFail(name: String) = false
}
