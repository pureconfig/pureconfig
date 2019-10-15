/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import com.typesafe.config._
import java.net.URL
import org.scalatest._

import pureconfig.error._
import pureconfig.generic.error._
import java.nio.file.Path

/**
 * Suite of tests related to the pretty printing of config reader failures.
 */
class ConfigReaderFailuresPrettyPrintSuite extends FlatSpec with Matchers {
  "A ConfigReaderFailures prettyPrint method" should "print errors with a configurable identation" in {

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

  it should "be printable with failures organized by path" in {
    val failures = ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), None, "a"),
      List(
        ConvertFailure(KeyNotFound("b", Set()), None, ""),
        ConvertFailure(KeyNotFound("c", Set()), None, "")))

    failures.prettyPrint() shouldBe
      s"""|at the root:
          |  - Key not found: 'b'.
          |  - Key not found: 'c'.
          |at 'a':
          |  - Expected type NUMBER. Found STRING instead.""".stripMargin
  }

  it should "print errors that occur at the root of the config" in {
    val failures1 = ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), None, ""),
      List())

    failures1.prettyPrint() shouldBe
      s"""|at the root:
          |  - Expected type OBJECT. Found NUMBER instead.""".stripMargin

    val failures2 = ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), None, "conf"),
      List())

    failures2.prettyPrint() shouldBe
      s"""|at 'conf':
          |  - Expected type OBJECT. Found NUMBER instead.""".stripMargin
  }

  it should "print the full error path" in {
    val failures = ConfigReaderFailures(
      ConvertFailure(
        WrongType(ConfigValueType.STRING, Set(ConfigValueType.OBJECT)), None, "values.b"),
      List(ConvertFailure(WrongType(ConfigValueType.NUMBER, Set(ConfigValueType.OBJECT)), None, "values.a.values.c")))

    failures.prettyPrint() shouldBe
      s"""|at 'values.a.values.c':
          |  - Expected type OBJECT. Found NUMBER instead.
          |at 'values.b':
          |  - Expected type OBJECT. Found STRING instead.""".stripMargin
  }

  it should "print a message displaying relevant errors for coproduct derivation" in {
    val failures = ConfigReaderFailures(
      ConvertFailure(UnexpectedValueForFieldCoproductHint(ConfigValueFactory.fromAnyRef("unexpected")), None, "values.v1.type"),
      List(ConvertFailure(KeyNotFound("type", Set()), None, "values.v3")))

    failures.prettyPrint() shouldBe
      s"""|at 'values.v1.type':
          |  - Unexpected value "unexpected" found. Note that the default transformation for representing class names in config values changed from converting to lower case to converting to kebab case in version 0.11.0 of PureConfig. See https://pureconfig.github.io/docs/overriding-behavior-for-sealed-families.html for more details on how to use a different transformation.
          |at 'values.v3':
          |  - Key not found: 'type'.""".stripMargin
  }

  it should "print a message displaying candidate keys in case of a suspected misconfigured ProductHint" in {
    val failures = ConfigReaderFailures(
      ConvertFailure(KeyNotFound("camel-case-int", Set("camelCaseInt")), None, "camel-case-conf"),
      List(
        ConvertFailure(KeyNotFound("camel-case-string", Set("camelCaseString")), None, "camel-case-conf"),
        ConvertFailure(KeyNotFound("snake-case-int", Set("snake_case_int")), None, "snake-case-conf"),
        ConvertFailure(KeyNotFound("snake-case-string", Set("snake_case_string")), None, "snake-case-conf")))

    failures.prettyPrint() shouldBe
      s"""|at 'camel-case-conf':
          |  - Key not found: 'camel-case-int'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |     - 'camelCaseInt'
          |  - Key not found: 'camel-case-string'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |     - 'camelCaseString'
          |at 'snake-case-conf':
          |  - Key not found: 'snake-case-int'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |     - 'snake_case_int'
          |  - Key not found: 'snake-case-string'. You might have a misconfigured ProductHint, since the following similar keys were found:
          |     - 'snake_case_string'""".stripMargin
  }

  it should "print a message displaying the proper file system location of the values that raised errors, if available" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/configFailureLocation/single/a.conf"
    val url = new URL("file://" + workingDir + file)

    val failures = ConfigReaderFailures(
      ConvertFailure(KeyNotFound("a", Set()), Some(ConfigValueLocation(url, 1)), ""),
      List(ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), Some(ConfigValueLocation(url, 3)), "c")))

    failures.prettyPrint() shouldBe
      s"""|at the root:
          |  - (file:${workingDir}${file}:1) Key not found: 'a'.
          |at 'c':
          |  - (file:${workingDir}${file}:3) Expected type NUMBER. Found STRING instead.""".stripMargin
  }

  it should "print a message displaying the inability to parse a given configuration" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/malformed/a.conf"
    val url = new URL("file://" + workingDir + file)

    val failures = ConfigReaderFailures(
      CannotParse("Expecting close brace } or a comma, got end of file", Some(ConfigValueLocation(url, 2))), List())

    failures.prettyPrint() shouldBe
      s"""|- (file:${workingDir}${file}:2) Unable to parse the configuration: Expecting close brace } or a comma, got end of file.""".stripMargin
  }

  it should "print a message indicating that a given file does not exist" in {
    val workingDir = getClass.getResource("/").getFile
    val file = "conf/nonexisting"
    val path = java.nio.file.Paths.get(workingDir + file)

    val failures = ConfigReaderFailures(
      CannotReadFile(path, Some(new java.io.FileNotFoundException(workingDir + file + " (No such file or directory)"))), List())

    failures.prettyPrint() shouldBe
      s"""|- Unable to read file ${workingDir}${file} (No such file or directory).""".stripMargin
  }

  it should "print a message showing lists of wrong size" in {
    val failures = ConfigReaderFailures(
      ConvertFailure(WrongSizeList(3, 4), None, "hlist"),
      List(ConvertFailure(WrongSizeList(3, 6), None, "tuple")))

    failures.prettyPrint() shouldBe
      s"""|at 'hlist':
          |  - List of wrong size found. Expected 3 elements. Found 4 elements instead.
          |at 'tuple':
          |  - List of wrong size found. Expected 3 elements. Found 6 elements instead.""".stripMargin
  }
}
