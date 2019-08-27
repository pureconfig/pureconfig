/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli (mario.pastorelli@teralitycs.ch)
 */
package pureconfig.example

import java.nio.file.Path

import pureconfig._
import pureconfig.example.conf._
import pureconfig.generic.auto._

/*
This is an example of configuration for our directory watcher. The configuration needs:
- the path that is the directory to watch
- a filter that will be used to decide if a path should be notified or not
- the email configuration used to send emails

The root namespace will be "dirwatch". For instance, valid property file for this
configuration will contain:

dirwatch.path="/path/to/observe"
dirwatch.filter="*"
dirwatch.email.host=host_of_email_service
dirwatch.email.port=12345
dirwatch.email.message="Dirwatch new path found report"
dirwatch.email.recipients=["recipient1@domain.tld","recipient2@domain.tld"]
dirwatch.email.sender="sender@domain.tld"
*/
object Main extends App {

  case class Config(dirwatch: DirWatchConfig)
  case class DirWatchConfig(path: Path, filter: String, email: EmailConfig)
  case class EmailConfig(host: String, port: Int, message: String, recipients: Set[Email], sender: Email)

  val config = ConfigSource.default.loadOrThrow[Config]

  println("dirwatch.path: " + config.dirwatch.path)
  println("dirwatch.filter: " + config.dirwatch.filter)
  println("dirwatch.email.host: " + config.dirwatch.email.host)
  println("dirwatch.email.port: " + config.dirwatch.email.port)
  println("dirwatch.email.message: " + config.dirwatch.email.message)
  println("dirwatch.email.recipients: " + config.dirwatch.email.recipients)
  println("dirwatch.email.sender: " + config.dirwatch.email.sender)

  println("The loaded configuration is: " ++ config.toString)

  // do amazing things with the config
}
