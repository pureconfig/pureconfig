/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import scala.collection.mutable
import scala.reflect.ClassTag

final case class ConfigReaderException[T](failures: ConfigReaderFailures)(implicit ct: ClassTag[T]) extends RuntimeException {

  override def getMessage: String = {
    val linesBuffer = mutable.Buffer.empty[String]
    linesBuffer += s"Cannot convert configuration to a ${ct.runtimeClass.getName}. Failures are:"

    val failuresList = failures.toList
    val parseFailures = failuresList.collect { case f: CannotParse => f }
    val convertFailures = failuresList.collect { case f: ConvertFailure => f }

    val failuresByPath = convertFailures.toList.groupBy(_.path).toList.sortBy(_._1)

    parseFailures.foreach { failure =>
      linesBuffer += s"  - ${ConfigReaderException.descriptionWithLocation(failure)}"
    }

    if (parseFailures.nonEmpty && convertFailures.nonEmpty) {
      linesBuffer += ""
    }

    failuresByPath.foreach {
      case (p, failures) =>
        linesBuffer += (if (p.isEmpty) s"  at the root:" else s"  at '$p':")
        failures.foreach { failure =>
          linesBuffer += s"    - ${ConfigReaderException.descriptionWithLocation(failure)}"
        }
    }

    linesBuffer += ""
    linesBuffer.mkString(System.lineSeparator())
  }

}

object ConfigReaderException {
  private[ConfigReaderException] def descriptionWithLocation(failure: ConfigReaderFailure): String =
    failure.location.fold(failure.description)(_.description + " " + failure.description)
}
