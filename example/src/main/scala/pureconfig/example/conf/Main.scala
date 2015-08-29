/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli (mario.pastorelli@teralitycs.ch)
 */
package pureconfig.example

import pureconfig._
import pureconfig.example.conf._

import scala.util.{Failure, Success}


object Main extends App {

  val config = loadConfig[Config] match {
    case Failure(f) => throw f
    case Success(conf) => conf
  }

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
