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

    val failuresByPath = failures.toList.groupBy(_.path)
    val failuresWithPath = (failuresByPath - None).map({ case (k, v) => k.get -> v }).toList.sortBy(_._1)
    val failuresWithoutPath = failuresByPath.getOrElse(None, Nil)

    if (failuresWithoutPath.nonEmpty)
      linesBuffer += "  in the configuration:"

    failuresWithoutPath.foreach { failure =>
      linesBuffer += s"${ConfigReaderException.descriptionWithLocation(failure)}"
    }

    if (failuresWithPath.nonEmpty && failuresWithoutPath.nonEmpty) {
      linesBuffer += ""
    }

    failuresWithPath.foreach {
      case (p, failures) =>
        linesBuffer += s"  at '$p':"
        failures.foreach { failure =>
          linesBuffer += s"${ConfigReaderException.descriptionWithLocation(failure)}"
        }
    }

    linesBuffer += ""
    linesBuffer.mkString(System.lineSeparator())
  }

}

object ConfigReaderException {
  private[ConfigReaderException] def descriptionWithLocation(failure: ConfigReaderFailure): String = {
    val failureLines = failure.description.split("\n")
    (failure.location.fold("    - " + failureLines.head)("    - " + _.description + " " + failureLines.head) ::
      failureLines.tail.map("      " + _).toList).mkString("\n")
  }
}
