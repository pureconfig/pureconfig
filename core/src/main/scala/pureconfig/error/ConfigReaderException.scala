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
    val (convertFailures, otherFailures) = failuresList.partition(_.isInstanceOf[ConvertFailure])

    val failuresByPath = convertFailures.asInstanceOf[List[ConvertFailure]].groupBy(_.path).toList.sortBy(_._1)

    otherFailures.foreach { failure =>
      linesBuffer += s"${ConfigReaderException.descriptionWithLocation(failure, "  ")}"
    }

    if (otherFailures.nonEmpty && convertFailures.nonEmpty) {
      linesBuffer += ""
    }

    failuresByPath.foreach {
      case (p, failures) =>
        linesBuffer += (if (p.isEmpty) s"  at the root:" else s"  at '$p':")
        failures.foreach { failure =>
          linesBuffer += s"${ConfigReaderException.descriptionWithLocation(failure, "    ")}"
        }
    }

    linesBuffer += ""
    linesBuffer.mkString(System.lineSeparator())
  }

}

object ConfigReaderException {
  private[ConfigReaderException] def descriptionWithLocation(failure: ConfigReaderFailure, prefix: String): String = {
    val failureLines = failure.description.split("\n")
    (failure.location.fold(s"${prefix}- ${failureLines.head}")(f => s"${prefix}- ${f.description} ${failureLines.head}") ::
      failureLines.tail.map(l => s"$prefix  $l").toList).mkString("\n")
  }
}
