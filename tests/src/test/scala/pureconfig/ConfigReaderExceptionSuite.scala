/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import com.typesafe.config._
import java.nio.file.Paths
import org.scalatest._
import shapeless._

import pureconfig.error._
import pureconfig.generic.auto._
import pureconfig.generic.hlist._
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
          |  at the root:
          |    - Key not found: 'b'.
          |    - Key not found: 'c'.
          |  at 'a':
          |    - Expected type NUMBER. Found STRING instead.
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
            type = "a-2"
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
          |  at 'values.v1.type':
          |    - Unexpected value "unexpected" found. You might have a misconfigured FieldCoproductHint. Note that the default transformation of FieldCoproductHint changed from converting to lower case to converting to kebab case in version 0.10.3.
          |  at 'values.v3':
          |    - Key not found: 'type'.
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
          |  at 'camel-case-conf':
          |    - Key not found: 'camel-case-int'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'camelCaseInt'
          |    - Key not found: 'camel-case-string'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'camelCaseString'
          |  at 'snake-case-conf':
          |    - Key not found: 'snake-case-int'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'snake_case_int'
          |    - Key not found: 'snake-case-string'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |       - 'snake_case_string'
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
          |  at the root:
          |    - (file:${workingDir}${file}:1) Key not found: 'a'.
          |  at 'c':
          |    - (file:${workingDir}${file}:3) Expected type NUMBER. Found STRING instead.
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

  case class HListAndTupleConf(hlist: Int :: Int :: String :: HNil, tuple: (Int, Int, String))

  it should "have a message showing lists of wrong size" in {
    val conf = ConfigFactory.parseString("""
      {
        hlist = [1, 2, "three", 4]
        tuple = [1, 2, "three", 4, 5, 6]
      }
    """)

    val exception = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[HListAndTupleConf]
    }

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$HListAndTupleConf. Failures are:
          |  at 'hlist':
          |    - List of wrong size found. Expected 3 elements. Found 4 elements instead.
          |  at 'tuple':
          |    - List of wrong size found. Expected 3 elements. Found 6 elements instead.
          |""".stripMargin
  }
}
