package pureconfig.error

import scala.annotation.tailrec
import scala.collection.mutable

import com.typesafe.config.{ConfigValue, ConfigValueType}

/** A representation of a reason why a value failed to be converted.
  */
trait FailureReason {

  /** A human-readable description of the failure.
    */
  def description: String
}

/** A general reason given for the failure of a value to be converted to a desired type.
  *
  * @param value
  *   the value that was requested to be converted
  * @param toType
  *   the target type that the value was requested to be converted to
  * @param because
  *   the reason why the conversion was not possible
  */
final case class CannotConvert(value: String, toType: String, because: String) extends FailureReason {
  def description = s"Cannot convert '$value' to $toType: $because."
}

/** A failure reason given when there is a collision of keys with different semantics. This error is raised when a key
  * that should be used to disambiguate a coproduct is mapped to a field in a product.
  *
  * @param key
  *   the colliding key
  * @param existingValue
  *   the value of the key
  */
final case class CollidingKeys(key: String, existingValue: ConfigValue) extends FailureReason {
  def description =
    s"Key with value '{$existingValue.render(ConfigRenderOptions.concise)}' collides with a key necessary to disambiguate a coproduct."
}

/** A failure reason given when a key is missing from a `ConfigObject` or `ConfigList`.
  *
  * @param key
  *   the key that is missing
  * @param candidates
  *   a set of candidate keys that might correspond to the desired key in case of a misconfigured ProductHint
  */
final case class KeyNotFound(key: String, candidates: Set[String] = Set()) extends FailureReason {
  def description = {
    if (candidates.nonEmpty) {
      val descLines = mutable.ListBuffer[String]()
      descLines += s"Key not found: '$key'. You might have a misconfigured ProductHint, since the following similar keys were found:"
      candidates.foreach { candidate =>
        descLines += s"  - '$candidate'"
      }
      descLines.mkString("\n")
    } else {
      s"Key not found: '$key'."
    }
  }
}

object KeyNotFound {
  @tailrec
  private[this] def isSubsequence(s1: String, s2: String): Boolean = {
    if (s1.isEmpty)
      true
    else if (s2.isEmpty)
      false
    else if (s1.head == s2.head)
      isSubsequence(s1.tail, s2.tail)
    else
      isSubsequence(s1, s2.tail)
  }

  def forKeys(fieldName: String, keys: Iterable[String]): KeyNotFound = {
    val lcField = fieldName.toLowerCase.filter(c => c.isDigit || c.isLetter)
    val objectKeys = keys.map(f => (f, f.toLowerCase))
    val candidateKeys = objectKeys.filter(k => isSubsequence(lcField, k._2)).map(_._1).toSet
    KeyNotFound(fieldName, candidateKeys)
  }
}

/** A failure reason given when an unknown key is found in a `ConfigObject`. The failure is raised when a key of a
  * `ConfigObject` is not mapped into a field of a given type and the `allowUnknownKeys` property of the `ProductHint`
  * for the type in question is `false`.
  *
  * @param key
  *   the unknown key
  */
final case class UnknownKey(key: String) extends FailureReason {
  def description = s"Unknown key."
}

/** A failure reason given when a `ConfigValue` has the wrong type.
  *
  * @param foundType
  *   the `ConfigValueType` that was found
  * @param expectedTypes
  *   the `ConfigValueType`s that were expected
  */
final case class WrongType(foundType: ConfigValueType, expectedTypes: Set[ConfigValueType]) extends FailureReason {
  def description = s"""Expected type ${expectedTypes.mkString(" or ")}. Found $foundType instead."""
}

/** A failure reason given when an exception is thrown during a conversion.
  *
  * @param throwable
  *   the `Throwable` that was raised
  */
final case class ExceptionThrown(throwable: Throwable) extends FailureReason {
  def description = s"${throwable.getMessage}."
}

/** A failure reason given when an unexpected empty string is found.
  *
  * @param typ
  *   the type that was attempted to be converted to from an empty string
  */
final case class EmptyStringFound(typ: String) extends FailureReason {
  def description = s"Empty string found when trying to convert to $typ."
}

/** A failure reason given when a list of an unexpected size is found when attempting to read into an `HList`.
  *
  * @param expected
  *   the expected number of elements
  * @param found
  *   the number of elements found
  */
final case class WrongSizeList(expected: Int, found: Int) extends FailureReason {
  def description = s"List of wrong size found. Expected $expected elements. Found $found elements instead."
}

/** A failure reason given when a string is not of the expected size.
  *
  * @param expected
  *   the expected number of characters
  * @param found
  *   the number of characters found
  */
final case class WrongSizeString(expected: Int, found: Int) extends FailureReason {
  def description = s"String of wrong size found. Expected $expected characters. Found $found characters instead."
}

/** A failure reason given when a validation failed.
  *
  * @param description
  *   the validation failed description
  */
final case class ValidationFailed(description: String) extends FailureReason
