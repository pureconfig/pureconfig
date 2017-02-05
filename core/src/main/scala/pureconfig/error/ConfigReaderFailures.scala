/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

case class ConfigReaderFailures(head: ConfigReaderFailure, tail: Seq[ConfigReaderFailure]) {

  def toSeq: Seq[ConfigReaderFailure] = head +: tail

  def +(failure: ConfigReaderFailure): ConfigReaderFailures =
    new ConfigReaderFailures(failure, this.toSeq)

  def ++(that: ConfigReaderFailures): ConfigReaderFailures =
    new ConfigReaderFailures(head, tail ++ that.toSeq)
}

object ConfigReaderFailures {

  def apply(configReaderFailure: ConfigReaderFailure): ConfigReaderFailures =
    new ConfigReaderFailures(configReaderFailure, Seq.empty[ConfigReaderFailure])
}
