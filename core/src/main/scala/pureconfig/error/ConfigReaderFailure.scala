/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import java.io.FileNotFoundException
import java.nio.file.Path

import pureconfig.ConfigCursor

/**
 * A representation of a failure raised from reading a config. The failure contains an optional file system location of
 * the configuration that raised the failure.
 */
trait ConfigReaderFailure {

  /**
   * A human-readable description of the failure.
   */
  def description: String

  /**
   * The optional location of the failure.
   */
  def location: Option[ConfigValueLocation]
}

/**
 * A failure occurred when converting from a `ConfigValue` to a given type. The failure contains a path to the
 * `ConfigValue` that raised the error.
 *
 * @param reason the reason for the conversion failure
 * @param location the optional location of the failure
 * @param path the path to the `ConfigValue` that raised the error
 */
case class ConvertFailure(reason: FailureReason, location: Option[ConfigValueLocation], path: String)
  extends ConfigReaderFailure {

  def description = reason.description
}

object ConvertFailure {

  /**
   * Constructs a `ConvertFailure` from a reason and a `ConfigCursor`.
   *
   * @param reason the reason for the conversion failure
   * @param cur the cursor where the failure ocurred
   * @return a `ConvertFailure` for the given reason at the given cursor.
   */
  def apply(reason: FailureReason, cur: ConfigCursor): ConvertFailure =
    ConvertFailure(reason, cur.location, cur.path)
}

/**
 * A failure occurred because a list of files to load was empty.
 */
case object NoFilesToRead extends ConfigReaderFailure {
  def description = "The config files to load must not be empty."
  def location = None
}

/**
 * A failure occurred because an exception was thrown duing the reading process.
 *
 * @param throwable the exception thrown
 * @param location the optional location of the failure
 */
final case class ThrowableFailure(throwable: Throwable, location: Option[ConfigValueLocation])
  extends ConfigReaderFailure {

  def description = s"${throwable.getMessage}."
}

/**
 * A failure occurred due to the inability to read a requested file.
 *
 * @param path the file system path of the file that couldn't be read
 * @param reason an optional exception thrown when trying to read the file
 */
final case class CannotReadFile(path: Path, reason: Option[Throwable]) extends ConfigReaderFailure {
  def description = reason match {
    case Some(ex: FileNotFoundException) => s"Unable to read file ${ex.getMessage}." // a FileNotFoundException already includes the path
    case Some(ex) => s"Unable to read file $path (${ex.getMessage})."
    case None => s"Unable to read file $path."
  }

  def location = None
}

/**
 * A failure occurred due to the inability to parse the configuration.
 *
 * @param msg the error message from the parser
 * @param location the optional location of the failure
 */
final case class CannotParse(msg: String, location: Option[ConfigValueLocation]) extends ConfigReaderFailure {
  def description = s"Unable to parse the configuration: $msg."
}
