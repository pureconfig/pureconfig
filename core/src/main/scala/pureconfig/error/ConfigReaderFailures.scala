/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import scala.collection.mutable

/**
 * A non-empty list of ConfigReader failures
 */
case class ConfigReaderFailures(head: ConfigReaderFailure, tail: List[ConfigReaderFailure]) {

  def toList: List[ConfigReaderFailure] = head +: tail

  def +:(failure: ConfigReaderFailure): ConfigReaderFailures =
    new ConfigReaderFailures(failure, this.toList)

  def ++(that: ConfigReaderFailures): ConfigReaderFailures =
    new ConfigReaderFailures(head, tail ++ that.toList)

  def prettyPrint(identLevel: Int = 0, identSize: Int = 2): String = {
    def tabs(n: Int): String = " " * ((identLevel + n) * identSize)
    def descriptionWithLocation(failure: ConfigReaderFailure, ident: Int): String = {
      val failureLines = failure.description.split("\n")
      (failure.location.fold(s"${tabs(ident)}- ${failureLines.head}")(f => s"${tabs(ident)}- ${f.description} ${failureLines.head}") ::
        failureLines.tail.map(l => s"${tabs(ident + 1)}$l").toList).mkString("\n")
    }

    val linesBuffer = mutable.Buffer.empty[String]
    val (convertFailures, otherFailures) = this.toList.partition(_.isInstanceOf[ConvertFailure])

    val failuresByPath = convertFailures.asInstanceOf[List[ConvertFailure]].groupBy(_.path).toList.sortBy(_._1)

    otherFailures.foreach { failure =>
      linesBuffer += descriptionWithLocation(failure, 0)
    }

    if (otherFailures.nonEmpty && convertFailures.nonEmpty) {
      linesBuffer += ""
    }

    failuresByPath.foreach {
      case (p, failures) =>
        linesBuffer += (tabs(0) + (if (p.isEmpty) s"at the root:" else s"at '$p':"))
        failures.foreach { failure =>
          linesBuffer += descriptionWithLocation(failure, 1)
        }
    }
    linesBuffer.mkString(System.lineSeparator())
  }
}

object ConfigReaderFailures {

  def apply(configReaderFailure: ConfigReaderFailure): ConfigReaderFailures =
    new ConfigReaderFailures(configReaderFailure, List.empty[ConfigReaderFailure])
}
