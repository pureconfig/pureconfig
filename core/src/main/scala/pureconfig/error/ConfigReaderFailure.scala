/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import java.io.FileNotFoundException
import java.nio.file.Path

import scala.annotation.tailrec
import scala.collection.mutable

import com.typesafe.config.{ ConfigRenderOptions, ConfigValue, ConfigValueType }

/**
 * A representation of a failure that might be raised from reading a
 * ConfigValue. The failure contains an optional file system location of the
 * configuration that raised the failure.
 */
abstract class ConfigReaderFailure {
  /**
   * A human-readable description of the failure.
   */
  def description: String

  /**
   * The optional location of the ConfigReaderFailure.
   */
  def location: Option[ConfigValueLocation]

  /**
   * Improves the context of this failure with the key to the parent node and
   * its optional location.
   */
  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]): ConfigReaderFailure
}

/**
 * A representation of a failure that might be raised from converting from a
 * `ConfigValue` to a given type. The failure contains a path to the
 * `ConfigValue` that raised the error.
 */
abstract class ConvertFailure extends ConfigReaderFailure {
  /**
   * The path to the `ConfigValue` that raised the failure.
   */
  def path: String
}

/**
 * A failure representing the inability to convert a null value. Since a null
 * represents a missing value, the location of this failure is always at the
 * root (i.e. an empty string).
 *
 * @param candidates a set of candidate keys that might map to the desired value
 *                   in case of a misconfigured ProductHint
 */
final case class CannotConvertNull(candidates: Set[String] = Set()) extends ConvertFailure {
  val location = None
  val path = ""

  def description = "Cannot convert a null value."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    KeyNotFound(parentKey, parentLocation, candidates)
}

object CannotConvertNull {
  @tailrec
  private[this] def isSubsequence(s1: String, s2: String): Boolean = {
    if (s1.isEmpty())
      true
    else if (s2.isEmpty())
      false
    else if (s1.head == s2.head)
      isSubsequence(s1.tail, s2.tail)
    else
      isSubsequence(s1, s2.tail)
  }

  def apply(fieldName: String, keys: Iterable[String]): CannotConvertNull = {
    val lcField = fieldName.toLowerCase.filter(c => c.isDigit || c.isLetter)
    val objectKeys = keys.map(f => (f, f.toLowerCase))
    val candidateKeys = objectKeys.filter(k => isSubsequence(lcField, k._2)).map(_._1).toSet
    CannotConvertNull(candidateKeys)
  }
}

/**
 * A failure representing the inability to convert a given value to a desired type.
 *
 * @param value the value that was requested to be converted
 * @param toType the target type that the value was requested to be converted to
 * @param because the reason why the conversion was not possible
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 * @param path the path to the value that couldn't be converted
 */
final case class CannotConvert(value: String, toType: String, because: String, location: Option[ConfigValueLocation], path: String) extends ConvertFailure {

  def description = s"Cannot convert '$value' to $toType: $because."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}

/**
 * A failure representing a collision of keys with different semantics. This
 * error is raised when a key that should be used to disambiguate a coproduct is
 * mapped to a field in a product.
 *
 * @param key the colliding key
 * @param existingValue the value of the key
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class CollidingKeys(key: String, existingValue: ConfigValue, location: Option[ConfigValueLocation]) extends ConvertFailure {
  def path = key

  def description = s"Key with value '{$existingValue.render(ConfigRenderOptions.concise)}' collides with a key necessary to disambiguate a coproduct."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(key = parentKey + "." + key, location = location orElse parentLocation)
}

/**
 * A failure representing a key missing from a ConfigObject.
 *
 * @param key the key that is missing
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 * @param candidates a set of candidate keys that might correspond to the
 *                   desired key in case of a misconfigured ProductHint
 */
final case class KeyNotFound(key: String, location: Option[ConfigValueLocation], candidates: Set[String] = Set()) extends ConvertFailure {
  def path = key

  def description = {
    if (candidates.nonEmpty) {
      val descLines = mutable.ListBuffer[String]()
      descLines += "Key not found. You might have a misconfigured ProductHint, since the following similar keys were found:"
      candidates.foreach { candidate =>
        descLines += (s" - '$candidate'")
      }
      descLines.mkString("\n")
    } else {
      "Key not found."
    }
  }

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(
      key = if (key.isEmpty) parentKey else parentKey + "." + key,
      location = location orElse parentLocation,
      candidates.map(parentKey + "." + _))
}

/**
 * A failure representing the presence of an unknown key in a ConfigObject. This
 * error is raised when a key of a ConfigObject is not mapped into a field of a
 * given type, and the allowUnknownKeys property of the ProductHint for the type
 * in question is false.
 *
 * @param key the unknown key
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class UnknownKey(key: String, location: Option[ConfigValueLocation]) extends ConvertFailure {
  def path = key

  def description = s"Unknown key."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(key = parentKey + "." + key, location = location orElse parentLocation)
}

/**
 * A failure representing a wrong type of a given ConfigValue.
 *
 * @param foundType the ConfigValueType that was found
 * @param expectedTypes the ConfigValueTypes that were expected
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 * @param path the path to the value that had a wrong type
 */
final case class WrongType(foundType: ConfigValueType, expectedTypes: Set[ConfigValueType], location: Option[ConfigValueLocation], path: String) extends ConvertFailure {
  def description = s"""Expected type ${expectedTypes.mkString(" or ")}. Found $foundType instead."""

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}

/**
 * A failure that resulted in a Throwable being raised.
 *
 * @param throwable the Throwable that was raised
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 * @param path the path to the value that raised the Throwable
 */
final case class ThrowableFailure(throwable: Throwable, location: Option[ConfigValueLocation], path: String) extends ConvertFailure {
  def description = s"${throwable.getMessage}."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}

/**
 * A failure representing an unexpected empty string.
 *
 * @param typ the type that was attempted to be converted to from an empty string
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 * @param path the path to the value which was an unexpected empty string
 */
final case class EmptyStringFound(typ: String, location: Option[ConfigValueLocation], path: String) extends ConvertFailure {
  def description = s"Empty string found when trying to convert to $typ."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}

/**
 * A failure representing an unexpected non-empty object when using `EnumCoproductHint` to write a config.
 *
 * @param typ the type for which a non-empty object was attempted to be written
 * @param location an optional location of the ConfigValue that raised the failure
 * @param path the path to the value which was an unexpected empty string
 */
final case class NonEmptyObjectFound(typ: String, location: Option[ConfigValueLocation], path: String) extends ConvertFailure {
  def description = s"Non-empty object found when using EnumCoproductHint to write a $typ."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}

/**
 * A failure representing that a list of an unexpected size was found when attempting to read into an HList.
 *
 * @param expected the expected number of elements
 * @param found the number of elements found
 * @param location an optional location of the ConfigValue that raised the failure
 * @param path the path to the value which was a list of an unexpected size
 */
final case class WrongSizeList(expected: Int, found: Int, location: Option[ConfigValueLocation], path: String) extends ConvertFailure {
  def description = s"List of wrong size found. Expected $expected elements. Found $found elements instead."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}

/**
 * A failure representing the inability to find a valid choice for a given coproduct.
 *
 * @param value the ConfigValue that was unable to be mapped to a coproduct choice
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 * @param path the path to the value who doesn't have a valid choice for a
 *             coproduct
 */
final case class NoValidCoproductChoiceFound(value: ConfigValue, location: Option[ConfigValueLocation], path: String) extends ConvertFailure {
  def description = s"No valid coproduct choice found for '${value.render(ConfigRenderOptions.concise())}'."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation, path = if (path.isEmpty) parentKey else parentKey + "." + path)
}

/**
 * A failure representing the inability to read a requested file.
 */
final case class CannotReadFile(path: Path, reason: Option[Throwable]) extends ConfigReaderFailure {
  val location = None

  def description = reason match {
    case Some(ex: FileNotFoundException) => s"Unable to read file ${ex.getMessage}." // a FileNotFoundException already includes the path
    case Some(ex) => s"Unable to read file $path (${ex.getMessage})."
    case None => s"Unable to read file $path."
  }

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) = this
}

/**
 * A failure representing the inability to parse the configuration.
 *
 * @param msg the error message from the parser
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class CannotParse(msg: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure {
  def description = s"Unable to parse the configuration: $msg."

  def withImprovedContext(parentKey: String, parentLocation: Option[ConfigValueLocation]) =
    this.copy(location = location orElse parentLocation)
}
