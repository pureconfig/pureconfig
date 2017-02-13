/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli (mario.pastorelli@teralitycs.ch)
 */
package pureconfig.example

import java.nio.file.{Path, Paths}

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.fromString

import scala.util.Try

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
package object conf {

  case class Config(dirwatch: DirWatchConfig)
  case class DirWatchConfig(path: Path, filter: String, email: EmailConfig)
  case class EmailConfig(host: String, port: Int, message: String, recipients: Set[Email], sender: Email)

  // Email doesn't have a Convert instance, we are going to create it here
  implicit val emailConvert: ConfigConvert[Email] = fromString[Email](Email.fromString)

  // XXX: Temporary fix until 0.6.0 is published, which has a built-in converter for Path
  implicit val pathConvert: ConfigConvert[Path] = fromString[Path](s => Try(Paths.get(s)))
}
