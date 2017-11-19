/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.net.URL

import com.typesafe.config.ConfigFactory
import org.scalatest._

import pureconfig.error._

/**
 * Suite of tests related to the location of ConfigValues that raised failures.
 */
class ConfigReaderFailureLocationSuite extends FlatSpec with Matchers with EitherValues {
  "Loading configuration from files" should "show proper error locations when loading a single file" in {
    import pureconfig.syntax._
    case class Conf(a: Int, b: String, c: Int)

    val workingDir = getClass.getResource("/").getFile
    val file = "conf/configFailureLocation/single/a.conf"
    val conf = ConfigFactory.load(file).root()

    val failures1 = conf.get("conf").to[Conf].left.value.toList
    failures1 should have size 2
    failures1 should contain(ConvertFailure(
      KeyNotFound("a"),
      Some(ConfigValueLocation(new URL("file", "", workingDir + file), 1)),
      ""))
    failures1 should contain(ConvertFailure(
      CannotConvert("hello", "Int", """java.lang.NumberFormatException: For input string: "hello""""),
      Some(ConfigValueLocation(new URL("file", "", workingDir + file), 3)),
      "c"))

    val failures2 = conf.get("other-conf").to[Conf].left.value.toList
    failures2 should have size 3
    failures2 should contain(ConvertFailure(
      KeyNotFound("a"),
      Some(ConfigValueLocation(new URL("file", "", workingDir + file), 7)),
      ""))
    failures2 should contain(ConvertFailure(
      KeyNotFound("b"),
      Some(ConfigValueLocation(new URL("file", "", workingDir + file), 7)),
      ""))
    failures2 should contain(ConvertFailure(
      CannotConvert("hello", "Int", """java.lang.NumberFormatException: For input string: "hello""""),
      Some(ConfigValueLocation(new URL("file", "", workingDir + file), 9)),
      "c"))
  }

  it should "show proper error location when loading from multiple files" in {
    import pureconfig.syntax._
    case class Conf(a: Int, b: String, c: Int)

    val workingDir = getClass.getResource("/").getFile
    val file1 = "conf/configFailureLocation/multiple/a.conf"
    val file2 = "conf/configFailureLocation/multiple/b.conf"
    val conf = ConfigFactory.load(file1).withFallback(ConfigFactory.load(file2)).root()

    val failures = conf.get("conf").to[Conf].left.value.toList
    failures should have size 1
    failures should contain(ConvertFailure(
      CannotConvert("string", "Int", """java.lang.NumberFormatException: For input string: "string""""),
      Some(ConfigValueLocation(new URL("file", "", workingDir + file2), 2)),
      "a"))
  }
}
