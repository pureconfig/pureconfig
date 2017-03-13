/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import com.typesafe.config.{ ConfigOrigin, ConfigValue }

/**
 * The physical location of a ConfigValue, represented by a file name and a line
 * number
 *
 * @param filename the complete pathname to the file that contains a given
 *                 ConfigValue
 * @param lineNumber the line number (starting at 0), where the given
 *                   ConfigValue definition starts
 */
case class ConfigValueLocation(filename: String, lineNumber: Int) {
  def description: String = s"($filename:$lineNumber)"
}

object ConfigValueLocation {
  /**
   * Helper method to create an optional ConfigValueLocation from a ConfigValue.
   * Since it might not be possible to derive a ConfigValueLocation from a
   * ConfigValue, this method returns Option.
   *
   * @param cv the ConfigValue to derive the location from
   *
   * @return a Some with the location of the ConfigValue, or None if it is not
   *         possible to derive a location. It is not possible to derive
   *         locations from ConfigValues that are not in files or for
   *         ConfigValues that are null.
   */
  def apply(cv: ConfigValue): Option[ConfigValueLocation] =
    Option(cv).flatMap(v => apply(v.origin()))

  /**
   * Helper method to create an optional ConfigValueLocation from a ConfigOrigin.
   * Since it might not be possible to derive a ConfigValueLocation from a
   * ConfigOrigin, this method returns Option.
   *
   * @param co the ConfigOrigin to derive the location from
   *
   * @return a Some with the location of the ConfigOrigin, or None if it is not
   *         possible to derive a location. It is not possible to derive
   *         locations from ConfigOrigin that are not in files or for
   *         ConfigOrigin that are null.
   */
  def apply(co: ConfigOrigin): Option[ConfigValueLocation] =
    Option(co).flatMap { origin =>
      if (origin.filename != null && origin.lineNumber != -1)
        Some(ConfigValueLocation(origin.filename, origin.lineNumber))
      else
        None
    }
}

/**
 * A representation of a failure that might be raised from reading a
 * ConfigValue. The failure contains an optional location of the ConfigValue
 * that raised the error.
 */
sealed abstract class ConfigReaderFailure {
  /**
   * The optional location of the ConfigReaderFailure
   */
  def location: Option[ConfigValueLocation]
}

/**
 * A failure representing the inability to convert a null value. Since a null
 * represents a missing value, the location of this failure is always None.
 */
final case object CannotConvertNull extends ConfigReaderFailure {
  val location = None
}

/**
 * A failure representing the inability to convert a given value to a desired type.
 *
 * @param value the value that was requested to be converted
 * @param toType the target type that the value was requested to be converted to
 * @param because the reason why the conversion was not possible
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class CannotConvert(value: String, toTyp: String, because: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

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
final case class CollidingKeys(key: String, existingValue: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

/**
 * A failure representing a key missing from a ConfigObject.
 *
 * @param key the key that is missing
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class KeyNotFound(key: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

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
final case class UnknownKey(key: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

/**
 * A failure representing a wrong type of a given ConfigValue.
 *
 * @param foundTyp the type of the ConfigValue that was found
 * @param expectedTyp the type of ConfigValue that was expected
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class WrongType(foundTyp: String, expectedTyp: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

/**
 * A failure representing a wrong type of a given ConfigValue. Similar to
 * [[WrongType]] but enriched with the key to the field that raised the error.
 *
 * @param foundTyp the type of the ConfigValue that was found
 * @param expectedTyp the type of ConfigValue that was expected
 * @param key the key to the field that raised the error
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class WrongTypeForKey(foundTyp: String, expectedTyp: String, key: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

/**
 * A failure that resulted in a Throwable being raised.
 *
 * @param throwable the Throwable that was raised
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class ThrowableFailure(throwable: Throwable, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

/**
 * A failure representing an unexpected empty string
 *
 * @param typ the type that was attempted to be converted to from an empty string
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class EmptyStringFound(typ: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

/**
 * A failure representing the inability to find a valid choice for a given coproduct.
 *
 * @param value the ConfigValue that was unable to be mapped to a coproduct choice
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class NoValidCoproductChoiceFound(value: ConfigValue, location: Option[ConfigValueLocation]) extends ConfigReaderFailure

/**
 * A failure representing the inability to parse the configuration.
 *
 * @param msg the error message from the parser
 * @param location an optional location of the ConfigValue that raised the
 *                 failure
 */
final case class CannotParse(msg: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure