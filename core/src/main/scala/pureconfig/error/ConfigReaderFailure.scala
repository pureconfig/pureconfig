/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import com.typesafe.config.ConfigValue

sealed abstract class ConfigReaderFailure
final case object CannotConvertNull extends ConfigReaderFailure
final case class CannotConvert(value: String, toTyp: String, because: String) extends ConfigReaderFailure
final case class CollidingKeys(key: String, existingValue: String) extends ConfigReaderFailure
final case class KeyNotFound(key: String) extends ConfigReaderFailure
final case class UnknownKey(key: String) extends ConfigReaderFailure
final case class WrongType(foundTyp: String, expectedTyp: String) extends ConfigReaderFailure
final case class WrongTypeForKey(typ: String, expectedTyp: String, key: String) extends ConfigReaderFailure
final case class ThrowableFailure(throwable: Throwable) extends ConfigReaderFailure
final case class EmptyStringFound(typ: String) extends ConfigReaderFailure
final case class NoValidCoproductChoiceFound(value: ConfigValue) extends ConfigReaderFailure