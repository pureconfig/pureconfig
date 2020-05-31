package pureconfig.generic

import com.typesafe.config.{ ConfigObject, ConfigValue, ConfigValueType }
import pureconfig._
import pureconfig.error._
import pureconfig.generic.CoproductHint.Use
import pureconfig.generic.error.{ CoproductHintException, NoValidCoproductOptionFound }
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

  def from(cursor: ConfigCursor, options: Seq[String]): ConfigReader.Result[CoproductHint.Action] =
    cursor.asString.right.flatMap { str =>
      options.find(str == fieldValue(_)) match {
        case Some(opt) => Right(Use(cursor, opt))
        case None => cursor.failed[CoproductHint.Action](NoValidCoproductOptionFound(cursor.value))
      }
    }

  def to(value: ConfigValue, name: String): ConfigValue =
    value match {
      case co: ConfigObject if co.isEmpty => fieldValue(name).toConfig
      case _: ConfigObject =>
        throw CoproductHintException(NonEmptyObjectFound(name))
      case cv =>
        throw CoproductHintException(WrongType(cv.valueType, Set(ConfigValueType.OBJECT)))
    }
}
