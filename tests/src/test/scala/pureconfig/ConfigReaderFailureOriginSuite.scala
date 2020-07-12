/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.net.URL

import com.typesafe.config.{ConfigFactory, ConfigValueType}
import org.scalatest.{EitherValues, Inside}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.error._
import pureconfig.generic.auto._

/**
  * Suite of tests related to the origin of ConfigValues that raised failures.
  */
class ConfigReaderFailureOriginSuite extends BaseSuite with EitherValues with Inside {
  "Loading configuration from files" should "show proper error locations when loading a single file" in {
    import pureconfig.syntax._
    case class Conf(a: Int, b: String, c: Int)

    val workingDir = getClass.getResource("/").getFile
    val file = "conf/configFailureOrigin/single/a.conf"
    val conf = ConfigFactory.load(file).root()

    inside(conf.get("conf").to[Conf].left.value.toList) {
      case List(
            ConvertFailure(KeyNotFound("a", _), Some(origin1), ""),
            ConvertFailure(WrongType(ConfigValueType.STRING, valueTypes), Some(origin2), "c")
          ) =>
        origin1.filename() should endWith(file)
        origin1.url() shouldBe new URL("file", "", workingDir + file)
        origin1.lineNumber() shouldBe 1

        origin2.filename() should endWith(file)
        origin2.url() shouldBe new URL("file", "", workingDir + file)
        origin2.lineNumber() shouldBe 3
        valueTypes should contain only ConfigValueType.NUMBER

    }

    inside(conf.get("other-conf").to[Conf].left.value.toList) {
      case List(
            ConvertFailure(KeyNotFound("a", _), Some(origin1), ""),
            ConvertFailure(KeyNotFound("b", _), Some(origin2), ""),
            ConvertFailure(WrongType(ConfigValueType.STRING, valueTypes2), Some(origin3), "c")
          ) =>
        origin1.filename() should endWith(file)
        origin1.url shouldBe new URL("file", "", workingDir + file)
        origin1.lineNumber shouldBe 7

        origin2.filename() should endWith(file)
        origin2.url shouldBe new URL("file", "", workingDir + file)
        origin2.lineNumber shouldBe 7

        origin3.filename() should endWith(file)
        origin3.url shouldBe new URL("file", "", workingDir + file)
        origin3.lineNumber shouldBe 9
        valueTypes2 should contain only ConfigValueType.NUMBER
    }
  }

  it should "show proper error location when loading from multiple files" in {
    import pureconfig.syntax._
    case class Conf(a: Int, b: String, c: Int)

    val workingDir = getClass.getResource("/").getFile
    val file1 = "conf/configFailureOrigin/multiple/a.conf"
    val file2 = "conf/configFailureOrigin/multiple/b.conf"
    val conf = ConfigFactory.load(file1).withFallback(ConfigFactory.load(file2)).root()

    inside(conf.get("conf").to[Conf].left.value.toList) {
      case List(ConvertFailure(WrongType(ConfigValueType.STRING, valueTypes), Some(origin), "a")) =>
        valueTypes should contain only ConfigValueType.NUMBER
        origin.url() shouldBe new URL("file", "", workingDir + file2)
        origin.lineNumber() shouldBe 2

    }
  }
}
