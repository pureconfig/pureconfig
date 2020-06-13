/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import scala.collection.mutable

/**
 * A non-empty list of ConfigReader failures
 */
case class ConfigReaderFailures(head: ConfigReaderFailure, tail: ConfigReaderFailure*) {

  lazy val toList: List[ConfigReaderFailure] = head :: tail.toList

  def +:(failure: ConfigReaderFailure): ConfigReaderFailures =
    ConfigReaderFailures(failure, this.toList: _*)

  def ++(that: ConfigReaderFailures): ConfigReaderFailures =
    ConfigReaderFailures(head, (tail ++ that.toList): _*)

  def prettyPrint(identLevel: Int = 0, identSize: Int = 2): String = {
    def tabs(n: Int): String = " " * ((identLevel + n) * identSize)
    def descriptionWithOrigin(failure: ConfigReaderFailure, ident: Int): String = {
      val failureLines = failure.description.split("\n")
      (failure.origin.fold(s"${tabs(ident)}- ${failureLines.head}")(f => s"${tabs(ident)}- (${f.description}) ${failureLines.head}") ::
        failureLines.tail.map(l => s"${tabs(ident + 1)}$l").toList).mkString("\n")
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
}
