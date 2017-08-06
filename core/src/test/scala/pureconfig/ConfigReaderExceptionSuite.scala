/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import com.typesafe.config._
import java.nio.file.Paths
import org.scalatest._
import shapeless._

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
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  at 'a':
          |    - Cannot convert 'string' to Int: java.lang.NumberFormatException: For input string: "string".
          |  at 'b':
          |    - Key not found.
          |  at 'c':
          |    - Key not found.
          |""".stripMargin
  }

  case class ParentConf(conf: Conf)

  it should "have a message displaying errors that occur at the root of the configuration" in {
    val conf = ConfigFactory.parseString("""
      {
        conf = 2
      }
    """)

    val exception1 = intercept[ConfigReaderException[_]] {
      conf.root().get("conf").toOrThrow[Conf]
    }

    exception1.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  at the root:
          |    - Expected type OBJECT. Found NUMBER instead.
          |""".stripMargin

    val exception2 = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[ParentConf]
    }

    exception2.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$ParentConf. Failures are:
          |  at 'conf':
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
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$MapConf. Failures are:
          |  at 'values.a.values.c':
          |    - Expected type OBJECT. Found NUMBER instead.
          |  at 'values.b':
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
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$EnclosingA. Failures are:
          |  at 'values.v1':
          |    - No valid coproduct choice found for '{"a":2,"type":"unexpected"}'.
          |  at 'values.v3.type':
          |    - Key not found.
          |""".stripMargin
  }

  case class CamelCaseConf(camelCaseInt: Int, camelCaseString: String)
  case class KebabCaseConf(kebabCaseInt: Int, kebabCaseString: String)
  case class SnakeCaseConf(snakeCaseInt: Int, snakeCaseString: String)
  case class EnclosingConf(
    camelCaseConf: CamelCaseConf,
    kebabCaseConf: KebabCaseConf,
    snakeCaseConf: SnakeCaseConf)

  it should "have a message displaying candidate keys in case of a suspected misconfigured ProductHint" in {
    val conf = ConfigFactory.parseString("""{
      camel-case-conf {
        camelCaseInt = 2
        camelCaseString = "str"
      }
      kebab-case-conf {
        kebab-case-int = 2
        kebab-case-string = "str"
      }
      snake-case-conf {
        snake_case_int = 2
        snake_case_string = "str"
      }
    }""")

    val exception = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[EnclosingConf]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$EnclosingConf. Failures are:
          |  at 'camel-case-conf.camel-case-int':
          |    - Key not found. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'camel-case-conf.camelCaseInt'
          |  at 'camel-case-conf.camel-case-string':
          |    - Key not found. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'camel-case-conf.camelCaseString'
          |  at 'snake-case-conf.snake-case-int':
          |    - Key not found. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'snake-case-conf.snake_case_int'
          |  at 'snake-case-conf.snake-case-string':
          |    - Key not found. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'snake-case-conf.snake_case_string'
          |""".stripMargin
  }

  it should "have a message displaying the proper file system location of the values that raised errors, if available" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/configFailureLocation/single/a.conf"
    val conf = ConfigFactory.load(file).root()

    val exception = intercept[ConfigReaderException[_]] {
      conf.get("conf").toOrThrow[Conf]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  at 'a':
          |    - (file:${workingDir}${file}:1) Key not found.
          |  at 'c':
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
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  - (file:${workingDir}${file}:2) Unable to parse the configuration: Expecting close brace } or a comma, got end of file.
          |""".stripMargin
  }

  it should "have a message indicating that a given file does not exist" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/nonexisting"

    val exception = intercept[ConfigReaderException[_]] {
      loadConfigOrThrow[Conf](Paths.get(workingDir, file))
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  - Unable to read file ${workingDir}${file} (No such file or directory).
          |""".stripMargin
  }

  case class HListConf(v: (Int :: Int :: String :: HNil))

  it should "have a message showing lists of wrong size" in {
    val conf = ConfigFactory.parseString("""
      {
        v = [1, 2, "three", 4]
      }
    """)

    val exception = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[HListConf]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$HListConf. Failures are:
        |  at 'v':
        |    - List of wrong size found. Expected 3 elements. Found 4 elements instead.
        |""".stripMargin
  }
}
