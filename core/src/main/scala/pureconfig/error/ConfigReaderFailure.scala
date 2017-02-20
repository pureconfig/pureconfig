/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import com.typesafe.config.ConfigValue

/** The physical location of a config value, represented by a file name and a line number */
case class ConfigValueLocation(filename: String, lineNumber: Int)

object ConfigValueLocation {
  def apply(cv: ConfigValue): Option[ConfigValueLocation] =
    Option(cv).flatMap { v =>
      val origin = v.origin()
      if (origin.filename != null && origin.lineNumber != -1)
        Some(ConfigValueLocation(origin.filename, origin.lineNumber))
      else
        None
    }
}

sealed abstract class ConfigReaderFailure {
  def location: Option[ConfigValueLocation]
}

final case object CannotConvertNull extends ConfigReaderFailure {
  val location = None
}

final case class CannotConvert(value: String, toTyp: String, because: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class CollidingKeys(key: String, existingValue: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class KeyNotFound(key: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class UnknownKey(key: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class WrongType(foundTyp: String, expectedTyp: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class WrongTypeForKey(typ: String, expectedTyp: String, key: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class ThrowableFailure(throwable: Throwable, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class EmptyStringFound(typ: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
final case class NoValidCoproductChoiceFound(value: ConfigValue, location: Option[ConfigValueLocation]) extends ConfigReaderFailure
