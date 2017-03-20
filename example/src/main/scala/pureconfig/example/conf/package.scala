/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli (mario.pastorelli@teralitycs.ch)
 */
package pureconfig.example

import java.nio.file.{Path, Paths}

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.fromStringReaderTry

import scala.util.Try


package object conf {

  // Email doesn't have a Convert instance, we are going to create it here
  implicit val emailConvert: ConfigConvert[Email] = fromStringReaderTry[Email](Email.fromString)
}
