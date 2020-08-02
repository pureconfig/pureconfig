package pureconfig.module.yaml

import java.net.URLDecoder
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.{ConfigValue, ConfigValueType}
import org.scalatest.EitherValues
import pureconfig.BaseSuite
import pureconfig.error._
import pureconfig.generic.auto._
import pureconfig.module.yaml.error.NonStringKeyFound

@deprecated("Construct a `YamlConfigSource` pipeline instead", "0.12.1")
class YamlApiSuite extends BaseSuite with EitherValues {

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
    loadYaml[Conf](resourcePath("basic.yaml")) shouldBe Right(
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
    loadYaml[Conf](resourceContents("basic.yaml")) shouldBe Right(
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
    loadYaml[Conf](resourceContents("basic.yaml"), "") shouldBe Right(
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
    loadYaml[Conf](resourceContents("basic.yaml"), "foo") shouldBe Left(
      ConfigReaderFailures(ConvertFailure(KeyNotFound("foo", Set.empty), emptyConfigOrigin, ""))
    )
  }

  it should "fail to loadYaml from path with non existent namespace" in {
    loadYaml[Conf](resourcePath("basic.yaml"), "foo") shouldBe Left(
      ConfigReaderFailures(ConvertFailure(KeyNotFound("foo", Set.empty), emptyConfigOrigin, ""))
    )
  }

  it should "loadYaml from a path with empty namespace" in {
    loadYaml[Conf](resourcePath("basic.yaml"), "") shouldBe Right(
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
    loadYaml[Map[String, String]](
      resourceContents("basic.yaml"),
      "map"
    ).right.value should contain theSameElementsAs Map("a" -> "1.5", "b" -> "2.5", "c" -> "3.5")
  }

  it should "loadYaml from string content with a specific namespace of a BigInt" in {
    loadYaml[BigInt](resourceContents("basic.yaml"), "n").right.value shouldBe BigInt(
      "1234567890123456789012345678901234567890"
    )
  }

  it should "fail to loadYaml of an array from string content with a non existent specific namespace" in {
    loadYaml[BigInt](resourceContents("array.yaml"), "n") shouldBe Left(
      ConfigReaderFailures(
        ConvertFailure(WrongType(ConfigValueType.LIST, Set(ConfigValueType.OBJECT)), emptyConfigOrigin, "")
      )
    )
  }

  it should "loadYaml from a path with a specific namespace of a Map" in {
    loadYaml[Map[String, String]](resourcePath("basic.yaml"), "map").right.value should contain theSameElementsAs Map(
      "a" -> "1.5",
      "b" -> "2.5",
      "c" -> "3.5"
    )
  }

  it should "loadYaml from a path with a specific namespace of a BigInt" in {
    loadYaml[BigInt](resourceContents("basic.yaml"), "n").right.value shouldBe BigInt(
      "1234567890123456789012345678901234567890"
    )
  }

  it should "loadYamlOrThrow from a simple YAML file" in {
    loadYamlOrThrow[Conf](resourcePath("basic.yaml")) shouldBe Conf(
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
    loadYamlOrThrow[Conf](resourceContents("basic.yaml")) shouldBe Conf(
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
    loadYamls[List[InnerConf]](resourcePath("multi.yaml")) shouldBe Right(
      List(InnerConf(1, "abc"), InnerConf(2, "def"), InnerConf(3, "ghi"))
    )

    loadYamls[(InnerConf, Conf)](resourcePath("multi2.yaml")) shouldBe Right(
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
    loadYamls[(InnerConf, Conf)](resourceContents("multi2.yaml")) shouldBe Right(
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
    loadYamlsOrThrow[List[InnerConf]](resourcePath("multi.yaml")) shouldBe List(
      InnerConf(1, "abc"),
      InnerConf(2, "def"),
      InnerConf(3, "ghi")
    )

    loadYamlsOrThrow[(InnerConf, Conf)](resourcePath("multi2.yaml")) shouldBe (
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

    loadYamlsOrThrow[(InnerConf, Conf)](content) shouldBe (
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
    loadYaml[ConfigValue](Paths.get("nonexisting.yaml")) should failWithType[CannotReadFile]
    loadYamls[List[ConfigValue]](Paths.get("nonexisting.yaml")) should failWithType[CannotReadFile]
    a[ConfigReaderException[_]] should be thrownBy loadYamlOrThrow[ConfigValue](Paths.get("nonexisting.yaml"))
    a[ConfigReaderException[_]] should be thrownBy loadYamlsOrThrow[List[ConfigValue]](Paths.get("nonexisting.yaml"))
  }

  it should "fail with a domain error when a non-string key is found" in {
    loadYaml[ConfigValue](resourcePath("non_string_keys.yaml")) should failWithType[NonStringKeyFound]
    loadYaml[ConfigValue](resourceContents("non_string_keys.yaml")) should failWithType[NonStringKeyFound]
  }

  it should "fail with the correct config origin filled when a YAML fails to be parsed" in {
    loadYaml[ConfigValue](resourcePath("illegal.yaml")) should failWith(
      CannotParse("mapping values are not allowed here", urlConfigOrigin(resourcePath("illegal.yaml").toUri.toURL, 3))
    )

    loadYaml[ConfigValue](resourceContents("illegal.yaml")) should failWith(
      CannotParse("mapping values are not allowed here", None)
    )
  }
}
