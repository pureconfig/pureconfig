/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.example

import scala.util.{Failure, Success, Try}

/** This is not a production-quality email address validator. It is provided only for illustration purposes.
  */
object Email {
  private val regex = """^([a-zA-Z0-9!#$%&'.*+/=?^_`{|}~;-]+)@([a-zA-Z0-9.-]+)$""".r

  def fromString(str: String): Try[Email] =
    str match {
      case regex(local, domain) => Success(new Email(s"$local@$domain"))
      case _ => Failure(new IllegalArgumentException(s"$str is not a valid email address"))
    }
}

class Email private (address: String) {
  override def toString: String = address
}
