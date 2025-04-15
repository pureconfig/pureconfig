/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Path

import com.typesafe.config.ConfigOrigin

import pureconfig.ConfigCursor

/** A representation of a failure raised from reading a config. The failure contains an optional file system location of
  * the configuration that raised the failure.
  */
trait ConfigReaderFailure {

  /** A human-readable description of the failure.
    */
  def description: String

  /** The optional origin of the failure.
    */
  def origin: Option[ConfigOrigin]
}

/** A failure occurred when converting from a `ConfigValue` to a given type. The failure contains a path to the
  * `ConfigValue` that raised the error.
  *
  * @param reason
  *   the reason for the conversion failure
  * @param origin
  *   the optional origin of the failure
  * @param path
  *   the path to the `ConfigValue` that raised the error
  */
case class ConvertFailure(reason: FailureReason, origin: Option[ConfigOrigin], path: String)
    extends ConfigReaderFailure {

  def description = reason.description
}

object ConvertFailure {

  /** Constructs a `ConvertFailure` from a reason and a `ConfigCursor`.
    *
    * @param reason
    *   the reason for the conversion failure
    * @param cur
    *   the cursor where the failure ocurred
    * @return
    *   a `ConvertFailure` for the given reason at the given cursor.
    */
  def apply(reason: FailureReason, cur: ConfigCursor): ConvertFailure =
    ConvertFailure(reason, cur.origin, cur.path)
}

/** A failure occurred because an exception was thrown during the reading process.
  *
  * @param throwable
  *   the exception thrown
  * @param origin
  *   the optional origin of the failure
  */
final case class ThrowableFailure(throwable: Throwable, origin: Option[ConfigOrigin]) extends ConfigReaderFailure {

  def description = s"${throwable.getMessage}."
}

/** A failure occurred due to the inability to read a requested source (such as a file, a resource or a network
  * location).
  */
trait CannotRead extends ConfigReaderFailure {

  /** The source name (like a file path or a URL)
    */
  def sourceName: String

  /** The source type
    */
  def sourceType: String

  /** An optional exception thrown when trying to read the source
    */
  def reason: Option[Throwable]

  def description =
    reason match {
      case Some(ex: FileNotFoundException) =>
        s"Unable to read $sourceType ${ex.getMessage}." // a FileNotFoundException already includes the path
      case Some(ex) => s"Unable to read $sourceType $sourceName (${ex.getMessage})."
      case None => s"Unable to read $sourceType $sourceName."
    }

  def origin: Option[ConfigOrigin] = None
}

/** A failure occurred due to the inability to read a requested file.
  *
  * @param path
  *   the file system path of the file that couldn't be read
  * @param reason
  *   an optional exception thrown when trying to read the file
  */
final case class CannotReadFile(path: Path, reason: Option[Throwable]) extends CannotRead {
  val sourceType = "file"
  def sourceName = path.toString
}

/** A failure occurred due to the inability to read a requested URL.
  *
  * @param url
  *   the URL that couldn't be read
  * @param reason
  *   an optional exception thrown when trying to read the URL
  */
final case class CannotReadUrl(url: URL, reason: Option[Throwable]) extends CannotRead {
  val sourceType = "URL"
  def sourceName = url.toString
}

/** A failure occurred due to the inability to read a requested resource.
  *
  * @param resourceName
  *   the resource that couldn't be read
  * @param reason
  *   an optional exception thrown when trying to read the resource
  */
final case class CannotReadResource(resourceName: String, reason: Option[Throwable]) extends CannotRead {
  val sourceType = "resource"
  def sourceName = resourceName
}

/** A failure occurred due to the inability to parse the configuration.
  *
  * @param msg
  *   the error message from the parser
  * @param origin
  *   the optional origin of the failure
  */
final case class CannotParse(msg: String, origin: Option[ConfigOrigin]) extends ConfigReaderFailure {
  def description = s"Unable to parse the configuration: $msg."
}
