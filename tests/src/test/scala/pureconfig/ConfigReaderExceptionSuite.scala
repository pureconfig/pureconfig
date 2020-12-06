/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.net.URL
import java.nio.file.Paths

import com.typesafe.config._
import org.scalatest.Inside
import pureconfig.ConfigReader.Result
import pureconfig.error._
import pureconfig.syntax._

class ConfigReaderExceptionSuite extends BaseSuite with Inside {
  behavior of "ConfigReaderException"

  case class Conf(a: Int, b: String, c: Int)
  implicit val confReader: ConfigReader[Conf] = ConfigReader.forProduct3("a", "b", "c")(Conf.apply)

  it should "have a message with failures organized by path" in {
    val conf = ConfigFactory.parseString("""
      {
        a = "string"
      }
    """)

    val exception = intercept[ConfigReaderException[_]] {
      conf.toOrThrow[Conf]
    }

    exception.failures.toList.toSet shouldBe Set(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), stringConfigOrigin(3), "a"),
      ConvertFailure(KeyNotFound("b", Set()), stringConfigOrigin(2), ""),
      ConvertFailure(KeyNotFound("c", Set()), stringConfigOrigin(2), "")
    )

    exception.getMessage shouldBe
      s"""|Cannot convert configuration to a pureconfig.ConfigReaderExceptionSuite$$Conf. Failures are:
          |  at the root:
          |    - (String: 2) Key not found: 'b'.
          |    - (String: 2) Key not found: 'c'.
          |  at 'a':
          |    - (String: 3) Expected type NUMBER. Found STRING instead.
          |""".stripMargin
  }

  case class ParentConf(conf: Conf)
  implicit val parentConfReader: ConfigReader[ParentConf] = ConfigReader.forProduct1("conf")(ParentConf.apply)

  it should "include failures that occur at the root of the configuration" in {
    val conf = ConfigFactory.parseString("""
      {
        conf = 2
      }
    """)

    val exception1 = intercept[ConfigReaderException[_]] {
      conf.root().get("conf").toOrThrow[Conf]
    }

    exception1.failures.toList.toSet shouldBe Set(
      ConvertFailure(WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), stringConfigOrigin(3), "")
    )

    val exception2 = intercept[ConfigReaderException[_]] {
      conf.root().toOrThrow[ParentConf]
    }

    exception2.failures.toList.toSet shouldBe Set(
      ConvertFailure(WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), stringConfigOrigin(3), "conf")
    )
  }

  case class MapConf(values: Map[String, MapConf])
  object MapConf {
    // FIXME: This is being used to that we can have a recursive definition in the `mapConfReader` below. This is not
    //        necessary in Scala 2.13+ if we use by-name implicits in the `ConfigReader.forProductN` implementations.
    def lazyReader(reader: => ConfigReader[MapConf]): ConfigReader[MapConf] = new ConfigReader[MapConf] {
      lazy val delegate = reader
      def from(cur: ConfigCursor): Result[MapConf] = delegate.from(cur)
    }
    implicit lazy val mapConfReader: ConfigReader[MapConf] = lazyReader(
      ConfigReader.forProduct1("values")(MapConf.apply)
    )
  }

  it should "include failures with the full error path" in {
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

    exception.failures.toList.toSet shouldBe Set(
      ConvertFailure(
        WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT)),
        stringConfigOrigin(12),
        "values.b"
      ),
      ConvertFailure(
        WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)),
        stringConfigOrigin(6),
        "values.a.values.c"
      )
    )
  }

  it should "have failures with the proper file system location of the values that raised errors, if available" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/configFailureOrigin/single/a.conf"
    val url = new URL("file://" + workingDir + file)
    val conf = ConfigFactory.load(file).root()

    val exception = intercept[ConfigReaderException[_]] {
      conf.get("conf").toOrThrow[Conf]
    }

    inside(exception.failures.toList) {
      case List(
            ConvertFailure(KeyNotFound("a", _), Some(origin1), ""),
            ConvertFailure(WrongType(ConfigValueType.STRING, types), Some(origin2), "c")
          ) =>
        origin1.url shouldBe url
        origin1.lineNumber shouldBe 1
        types should contain only ConfigValueType.NUMBER
        origin2.url shouldBe url
        origin2.lineNumber shouldBe 3

    }
  }

  it should "include failures regarding the inability to parse a given configuration" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/malformed/a.conf"
    val url = new URL("file://" + workingDir + file)

    val exception = intercept[ConfigReaderException[_]] {
      ConfigSource.file(Paths.get(workingDir, file)).loadOrThrow[Conf]
    }

    inside(exception.failures.toList) {
      case List(CannotParse("Expecting close brace } or a comma, got end of file", Some(origin))) =>
        origin.url() shouldBe url
        origin.lineNumber() shouldBe 2
    }
  }

  it should "include failures indicating that a given file does not exist" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/nonexisting"

    val exception = intercept[ConfigReaderException[_]] {
      ConfigSource.file(Paths.get(workingDir, file)).loadOrThrow[Conf]
    }

    // Note: exceptions can't be compared for equality
    exception.failures.toList.toString shouldBe
      s"List(CannotReadFile(${workingDir}${file},Some(java.io.FileNotFoundException: ${workingDir}${file} (No such file or directory))))"
  }
}
