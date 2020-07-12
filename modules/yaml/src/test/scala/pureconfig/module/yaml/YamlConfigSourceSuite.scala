package pureconfig.module.yaml

import java.net.URLDecoder
import java.nio.file.{Files, Path, Paths}
import java.time.Instant

import com.typesafe.config.{ConfigValue, ConfigValueType}
import org.scalatest.EitherValues
import pureconfig.{BaseSuite, ConfigSource}
import pureconfig.error._
import pureconfig.generic.auto._
import pureconfig.module.yaml.error.NonStringKeyFound

class YamlConfigSourceSuite extends BaseSuite with EitherValues {

  def resourcePath(path: String): Path =
    Paths.get(URLDecoder.decode(getClass.getResource("/" + path).getFile, "UTF-8"))

  def resourceContents(path: String): String =
    new String(Files.readAllBytes(resourcePath(path)))

  case class InnerConf(aa: Int, bb: String)
  case class Conf(
      str: String,
      b: Boolean,
      n: BigInt,
      data: String,
      opt: Option[Int],
      s: Set[Int],
      xs: List[Long],
      map: Map[String, Double],
      inner: InnerConf
  )

  behavior of "YAML loading"

  it should "loadYaml from a simple YAML file" in {
    YamlConfigSource.file(resourcePath("basic.yaml")).load[Conf] shouldBe Right(
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        "dGhpcyBpcyBhIG1lc3NhZ2U=",
        None,
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def")
      )
    )
  }

  it should "loadYaml from a string" in {
    YamlConfigSource.string(resourceContents("basic.yaml")).load[Conf] shouldBe Right(
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        "dGhpcyBpcyBhIG1lc3NhZ2U=",
        None,
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def")
      )
    )
  }

  it should "loadYaml from string content with empty namespace" in {
    YamlConfigSource.string(resourceContents("basic.yaml")).at("").load[Conf] shouldBe Right(
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        "dGhpcyBpcyBhIG1lc3NhZ2U=",
        None,
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def")
      )
    )
  }

  it should "fail to loadYaml from string content with non existent namespace" in {
    YamlConfigSource.string(resourceContents("basic.yaml")).at("foo").load[Conf] shouldBe Left(
      ConfigReaderFailures(ConvertFailure(KeyNotFound("foo", Set.empty), emptyConfigOrigin, ""))
    )
  }

  it should "fail to loadYaml from path with non existent namespace" in {
    YamlConfigSource.file(resourcePath("basic.yaml")).at("foo").load[Conf] shouldBe Left(
      ConfigReaderFailures(ConvertFailure(KeyNotFound("foo", Set.empty), emptyConfigOrigin, ""))
    )
  }

  it should "loadYaml from a path with empty namespace" in {
    YamlConfigSource.file(resourcePath("basic.yaml")).at("").load[Conf] shouldBe Right(
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        "dGhpcyBpcyBhIG1lc3NhZ2U=",
        None,
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def")
      )
    )
  }

  it should "loadYaml from string content with a specific namespace of a Map" in {
    YamlConfigSource
      .string(resourceContents("basic.yaml"))
      .at("map")
      .load[Map[String, String]]
      .right
      .value should contain theSameElementsAs Map("a" -> "1.5", "b" -> "2.5", "c" -> "3.5")
  }

  it should "loadYaml from string content with a specific namespace of a BigInt" in {
    YamlConfigSource.string(resourceContents("basic.yaml")).at("n").load[BigInt].right.value shouldBe BigInt(
      "1234567890123456789012345678901234567890"
    )
  }

  it should "fail to loadYaml of an array from string content with a non existent specific namespace" in {
    YamlConfigSource.string(resourceContents("array.yaml")).at("n").load[BigInt] shouldBe Left(
      ConfigReaderFailures(
        ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.OBJECT)), emptyConfigOrigin, "")
      )
    )
  }

  it should "loadYaml from a path with a specific namespace of a Map" in {
    YamlConfigSource
      .file(resourcePath("basic.yaml"))
      .at("map")
      .load[Map[String, String]]
      .right
      .value should contain theSameElementsAs Map("a" -> "1.5", "b" -> "2.5", "c" -> "3.5")
  }

  it should "loadYaml from a path with a specific namespace of a BigInt" in {
    YamlConfigSource.string(resourceContents("basic.yaml")).at("n").load[BigInt].right.value shouldBe BigInt(
      "1234567890123456789012345678901234567890"
    )
  }

  it should "not change date formats when parsing YAML documents" in {
    case class MyConf(myInstant: Instant)

    val yaml = """
      | my-instant: 2019-01-01T00:00:00Z
      |""".stripMargin

    YamlConfigSource.string(yaml).load[MyConf] shouldBe Right(MyConf(Instant.parse("2019-01-01T00:00:00Z")))
  }

  it should "loadYamlOrThrow from a simple YAML file" in {
    YamlConfigSource.file(resourcePath("basic.yaml")).loadOrThrow[Conf] shouldBe Conf(
      "abc",
      true,
      BigInt("1234567890123456789012345678901234567890"),
      "dGhpcyBpcyBhIG1lc3NhZ2U=",
      None,
      Set(4, 6, 8),
      List(10L, 10000L, 10000000L, 10000000000L),
      Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
      InnerConf(42, "def")
    )
  }

  it should "loadYamlOrThrow from a string" in {
    YamlConfigSource.string(resourceContents("basic.yaml")).loadOrThrow[Conf] shouldBe Conf(
      "abc",
      true,
      BigInt("1234567890123456789012345678901234567890"),
      "dGhpcyBpcyBhIG1lc3NhZ2U=",
      None,
      Set(4, 6, 8),
      List(10L, 10000L, 10000000L, 10000000000L),
      Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
      InnerConf(42, "def")
    )
  }

  it should "loadYamls from a multi-document YAML file" in {
    YamlConfigSource.file(resourcePath("multi.yaml")).multiDoc.load[List[InnerConf]] shouldBe Right(
      List(InnerConf(1, "abc"), InnerConf(2, "def"), InnerConf(3, "ghi"))
    )

    YamlConfigSource.file(resourcePath("multi2.yaml")).multiDoc.load[(InnerConf, Conf)] shouldBe Right(
      (
        InnerConf(1, "abc"),
        Conf(
          "abc",
          true,
          BigInt("1234567890123456789012345678901234567890"),
          "dGhpcyBpcyBhIG1lc3NhZ2U=",
          None,
          Set(4, 6, 8),
          List(10L, 10000L, 10000000L, 10000000000L),
          Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
          InnerConf(42, "def")
        )
      )
    )
  }

  it should "loadYamls from a string" in {
    YamlConfigSource.string(resourceContents("multi2.yaml")).multiDoc.load[(InnerConf, Conf)] shouldBe Right(
      (
        InnerConf(1, "abc"),
        Conf(
          "abc",
          true,
          BigInt("1234567890123456789012345678901234567890"),
          "dGhpcyBpcyBhIG1lc3NhZ2U=",
          None,
          Set(4, 6, 8),
          List(10L, 10000L, 10000000L, 10000000000L),
          Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
          InnerConf(42, "def")
        )
      )
    )
  }

  it should "loadYamlsOrThrow from a multi-document YAML file" in {
    YamlConfigSource.file(resourcePath("multi.yaml")).multiDoc.loadOrThrow[List[InnerConf]] shouldBe List(
      InnerConf(1, "abc"),
      InnerConf(2, "def"),
      InnerConf(3, "ghi")
    )

    YamlConfigSource.file(resourcePath("multi2.yaml")).multiDoc.loadOrThrow[(InnerConf, Conf)] shouldBe (
      (
        InnerConf(1, "abc"),
        Conf(
          "abc",
          true,
          BigInt("1234567890123456789012345678901234567890"),
          "dGhpcyBpcyBhIG1lc3NhZ2U=",
          None,
          Set(4, 6, 8),
          List(10L, 10000L, 10000000L, 10000000000L),
          Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
          InnerConf(42, "def")
        )
      )
    )
  }

  it should "loadYamlsOrThrow from a string" in {
    val content = new String(Files.readAllBytes(resourcePath("multi2.yaml")))

    YamlConfigSource.string(content).multiDoc.loadOrThrow[(InnerConf, Conf)] shouldBe (
      (
        InnerConf(1, "abc"),
        Conf(
          "abc",
          true,
          BigInt("1234567890123456789012345678901234567890"),
          "dGhpcyBpcyBhIG1lc3NhZ2U=",
          None,
          Set(4, 6, 8),
          List(10L, 10000L, 10000000L, 10000000000L),
          Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
          InnerConf(42, "def")
        )
      )
    )
  }

  it should "fail with a domain error when a file does not exist" in {
    YamlConfigSource.file(Paths.get("nonexisting.yaml")).load[ConfigValue] should failWithType[CannotReadFile]
    YamlConfigSource
      .file(Paths.get("nonexisting.yaml"))
      .multiDoc
      .load[List[ConfigValue]] should failWithType[CannotReadFile]
    a[ConfigReaderException[_]] should be thrownBy YamlConfigSource
      .file(Paths.get("nonexisting.yaml"))
      .loadOrThrow[ConfigValue]
    a[ConfigReaderException[_]] should be thrownBy YamlConfigSource
      .file(Paths.get("nonexisting.yaml"))
      .multiDoc
      .loadOrThrow[List[ConfigValue]]
  }

  it should "fail with a domain error when a non-string key is found" in {
    YamlConfigSource.file(resourcePath("non_string_keys.yaml")).load[ConfigValue] should failWithType[NonStringKeyFound]
    YamlConfigSource
      .string(resourceContents("non_string_keys.yaml"))
      .load[ConfigValue] should failWithType[NonStringKeyFound]
  }

  it should "fail with the correct config origin filled when a YAML fails to be parsed" in {
    YamlConfigSource.file(resourcePath("illegal.yaml")).load[ConfigValue] should failWith(
      CannotParse("mapping values are not allowed here", urlConfigOrigin(resourcePath("illegal.yaml").toUri.toURL, 3))
    )

    YamlConfigSource.string(resourceContents("illegal.yaml")).load[ConfigValue] should failWith(
      CannotParse("mapping values are not allowed here", None)
    )
  }

  it should "allow being converted to an object source" in {
    YamlConfigSource.file(resourcePath("basic.yaml")).load[Conf] shouldBe
      YamlConfigSource.file(resourcePath("basic.yaml")).asObjectSource.load[Conf]

    YamlConfigSource.file(resourcePath("array.yaml")).asObjectSource.load[Conf] should failWithType[WrongType]
  }

  it should "be mergeable with non-YAML configs" in {
    ConfigSource
      .string(s"{ str: bcd, map { a: 0.5, b: $${map.c} } }")
      .withFallback(YamlConfigSource.file(resourcePath("basic.yaml")).asObjectSource)
      .load[Conf] shouldBe Right(
      Conf(
        "bcd",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        "dGhpcyBpcyBhIG1lc3NhZ2U=",
        None,
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 0.5, "b" -> 3.5, "c" -> 3.5),
        InnerConf(42, "def")
      )
    )
  }
}
