/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import scala.collection.mutable

/**
  * A non-empty list of ConfigReader failures
  */
case class ConfigReaderFailures(head: ConfigReaderFailure, tail: ConfigReaderFailure*) {

  def toList: List[ConfigReaderFailure] = head :: tail.toList

  def +:(failure: ConfigReaderFailure): ConfigReaderFailures =
    ConfigReaderFailures(failure, this.toList: _*)

  def ++(that: ConfigReaderFailures): ConfigReaderFailures =
    ConfigReaderFailures(head, (tail ++ that.toList): _*)

  def prettyPrint(indentLevel: Int = 0): String = {
    def tabs(n: Int): String = " " * ((indentLevel + n) * 2)
    def descriptionWithOrigin(failure: ConfigReaderFailure, indent: Int): String = {
      val failureLines = failure.description.split("\n")
      (failure.origin.fold(s"${tabs(indent)}- ${failureLines.head}")(f =>
        s"${tabs(indent)}- (${f.description}) ${failureLines.head}"
      ) ::
        failureLines.tail.map(l => s"${tabs(indent + 1)}$l").toList).mkString("\n")
    }

    val linesBuffer = mutable.Buffer.empty[String]
    val (convertFailures, otherFailures) = this.toList.partition(_.isInstanceOf[ConvertFailure])

    val failuresByPath = convertFailures.asInstanceOf[List[ConvertFailure]].groupBy(_.path).toList.sortBy(_._1)

    otherFailures.foreach { failure =>
      linesBuffer += descriptionWithOrigin(failure, 0)
    }

    if (otherFailures.nonEmpty && convertFailures.nonEmpty) {
      linesBuffer += ""
    }

    failuresByPath.foreach {
      case (p, failures) =>
        linesBuffer += (tabs(0) + (if (p.isEmpty) s"at the root:" else s"at '$p':"))
        failures.foreach { failure =>
          linesBuffer += descriptionWithOrigin(failure, 1)
        }
    }
    linesBuffer.mkString(System.lineSeparator())
  }

  override def toString = toList.map(_.toString).mkString("ConfigReaderFailures(", ",", ")")
}
