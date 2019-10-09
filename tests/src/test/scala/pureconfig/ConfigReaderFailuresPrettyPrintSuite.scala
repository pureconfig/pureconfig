/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.net.URL

import org.scalatest._
import pureconfig.error._

/**
 * Suite of tests related to the pretty printing of config reader failures.
 */
class ConfigReaderFailuresPrettyPrintSuite extends FlatSpec with Matchers {
  "A list of failures" should "be printable with a configurable identation" in {

    def location(line: Int) = ConfigValueLocation(new URL("file:///tmp/config"), line)

    val failures = ConfigReaderFailures(
      ThrowableFailure(new Exception("Throwable error"), Some(location(12))),
      List(
        ConvertFailure(KeyNotFound("unknown_key"), None, "path"),
        CannotReadResource("resourceName", None)))

    failures.prettyPrint(0, 2) shouldBe
      s"""|- (file:/tmp/config:12) Throwable error.
          |- Unable to read resource resourceName.
          |
          |at 'path':
          |  - Key not found: 'unknown_key'.""".stripMargin

    failures.prettyPrint(1, 2) shouldBe
      s"""|  - (file:/tmp/config:12) Throwable error.
          |  - Unable to read resource resourceName.
          |
          |  at 'path':
          |    - Key not found: 'unknown_key'.""".stripMargin

    failures.prettyPrint(1, 4) shouldBe
      s"""|    - (file:/tmp/config:12) Throwable error.
          |    - Unable to read resource resourceName.
          |
          |    at 'path':
          |        - Key not found: 'unknown_key'.""".stripMargin
  }
}
