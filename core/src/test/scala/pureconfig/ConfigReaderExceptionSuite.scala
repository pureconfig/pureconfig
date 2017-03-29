/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import com.typesafe.config._
import java.nio.file.Paths
import org.scalatest._

import pureconfig.error._
import pureconfig.syntax._

class ConfigReaderExceptionSuite extends FlatSpec with Matchers {
  behavior of "ConfigReaderException"

  case class Conf(a: Int, b: String, c: Int)

  it should "have a message with failures organized by path" in {
    val conf = ConfigFactory.parseString("""
      {
        a = "string"
      }
    """)

    val exception = intercept[ConfigReaderException[_]] {
      conf.toOrThrow[Conf]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a value of class pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  with 'a':
          |    - Cannot convert 'string' to Int: java.lang.NumberFormatException: For input string: "string".
          |  with 'b':
          |    - Key not found.
          |  with 'c':
          |    - Key not found.
          |""".stripMargin
  }

  case class ParentConf(conf: Conf)

  it should "have a message displaying errors not associated with a given path" in {
    val conf = ConfigFactory.parseString("""
      {
        conf = 2
      }
    """)

    val exception1 = intercept[ConfigReaderException[_]] {
      conf.root().get("conf").toOrThrow[Conf]
    }

    exception1.getMessage shouldBe
      s"""|Cannot convert configuration to a value of class pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  in the configuration:
          |    - Expected type OBJECT. Found NUMBER instead.
          |""".stripMargin

    val exception2 = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[ParentConf]
    }

    exception2.getMessage shouldBe
      s"""|Cannot convert configuration to a value of class pureconfig.ConfigReaderExceptionSuite$$ParentConf. Failures are:
          |  with 'conf':
          |    - Expected type OBJECT. Found NUMBER instead.
          |""".stripMargin
  }

  case class MapConf(values: Map[String, MapConf])

  it should "have a message showing the full path to errors" in {
    val conf = ConfigFactory.parseString("""
      {
        values {
          a {
            values {
              c = 2
              d {
                values = {}
              }
            }
          }
          b = "str"
        }
      }
    """)

    val exception = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[MapConf]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a value of class pureconfig.ConfigReaderExceptionSuite$$MapConf. Failures are:
          |  with 'values.a.values.c':
          |    - Expected type OBJECT. Found NUMBER instead.
          |  with 'values.b':
          |    - Expected type OBJECT. Found STRING instead.
          |""".stripMargin
  }

  sealed trait A
  case class A1(a: Int) extends A
  case class A2(a: String) extends A
  case class EnclosingA(values: Map[String, A])

  it should "have a message displaying relevant errors for coproduct derivation" in {
    val conf = ConfigFactory.parseString("""
      {
        values {
          v1 {
            type = "unexpected"
            a = 2
          }
          v2 {
            type = "a2"
            a = "val"
          }
          v3 {
            a = 5
          }
        }
      }
    """)

    val exception = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[EnclosingA]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a value of class pureconfig.ConfigReaderExceptionSuite$$EnclosingA. Failures are:
          |  with 'values.v1':
          |    - No valid coproduct choice found for '{"a":2,"type":"unexpected"}'.
          |  with 'values.v3.type':
          |    - Key not found.
          |""".stripMargin
  }

  it should "have a message displaying the proper physical location of the values that raised errors, if available" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/configFailureLocation/single/a.conf"
    val conf = ConfigFactory.load(file).root()

    val exception = intercept[ConfigReaderException[_]] {
      conf.get("conf").toOrThrow[Conf]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a value of class pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  with 'a':
          |    - (file:${workingDir}${file}:1) Key not found.
          |  with 'c':
          |    - (file:${workingDir}${file}:3) Cannot convert 'hello' to Int: java.lang.NumberFormatException: For input string: "hello".
          |""".stripMargin
  }

  it should "have a message displaying the inability to parse a given configuration" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/malformed/a.conf"

    val exception = intercept[ConfigReaderException[_]] {
      loadConfigOrThrow[Conf](Paths.get(workingDir, file))
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a value of class pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  in the configuration:
          |    - (file:${workingDir}${file}:2) Unable to parse the configuration.
          |""".stripMargin
  }
}
