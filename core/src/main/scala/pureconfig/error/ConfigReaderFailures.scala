/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

/**
 * A non-empty list of ConfigReader failures
 */
case class ConfigReaderFailures(head: ConfigReaderFailure, tail: List[ConfigReaderFailure]) {

  def toList: List[ConfigReaderFailure] = head +: tail

  def +:(failure: ConfigReaderFailure): ConfigReaderFailures =
    new ConfigReaderFailures(failure, this.toList)

  def ++(that: ConfigReaderFailures): ConfigReaderFailures =
    new ConfigReaderFailures(head, tail ++ that.toList)
}

object ConfigReaderFailures {

  def apply(configReaderFailure: ConfigReaderFailure): ConfigReaderFailures =
    new ConfigReaderFailures(configReaderFailure, List.empty[ConfigReaderFailure])

  def apply(configReaderFailures: List[ConfigReaderFailure]): ConfigReaderFailures =
    new ConfigReaderFailures(configReaderFailures.head, configReaderFailures.tail)
}
