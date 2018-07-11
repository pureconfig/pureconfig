package pureconfig.module.yaml

import java.net.URLDecoder
import java.nio.file.{ Files, Path, Paths }

import com.typesafe.config.ConfigValue
import pureconfig.BaseSuite
import pureconfig.error.{ CannotParse, CannotReadFile, ConfigReaderException, ConfigValueLocation }
import pureconfig.module.yaml.error.NonStringKeyFound

class YamlApiSuite extends BaseSuite {

  def resourcePath(path: String): Path =
    Paths.get(URLDecoder.decode(getClass.getResource("/" + path).getFile, "UTF-8"))

  case class InnerConf(aa: Int, bb: String)
  case class Conf(
      str: String,
      b: Boolean,
      n: BigInt,
      s: Set[Int],
      xs: List[Long],
      map: Map[String, Double],
      inner: InnerConf)

  behavior of "YAML loading"

  it should "loadYaml from a simple YAML file" in {
    loadYaml[Conf](resourcePath("basic.yaml")) shouldBe Right(Conf(
      "abc",
      true,
      BigInt("1234567890123456789012345678901234567890"),
      Set(4, 6, 8),
      List(10L, 10000L, 10000000L, 10000000000L),
      Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
      InnerConf(42, "def")))

    loadYaml[Conf](Paths.get("nonexisting.yaml")) should failWithType[CannotReadFile]
  }

  it should "loadYaml from a string" in {
    val contents = new String(Files.readAllBytes(resourcePath("basic.yaml")))

    loadYaml[Conf](contents) shouldBe Right(Conf(
      "abc",
      true,
      BigInt("1234567890123456789012345678901234567890"),
      Set(4, 6, 8),
      List(10L, 10000L, 10000000L, 10000000000L),
      Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
      InnerConf(42, "def")))
  }

  it should "loadYamlOrThrow from a simple YAML file" in {
    loadYamlOrThrow[Conf](resourcePath("basic.yaml")) shouldBe Conf(
      "abc",
      true,
      BigInt("1234567890123456789012345678901234567890"),
      Set(4, 6, 8),
      List(10L, 10000L, 10000000L, 10000000000L),
      Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
      InnerConf(42, "def"))

    a[ConfigReaderException[_]] should be thrownBy loadYamlOrThrow[Conf](Paths.get("nonexisting.yaml"))
  }

  it should "loadYamlOrThrow from a string" in {
    val contents = new String(Files.readAllBytes(resourcePath("basic.yaml")))

    loadYamlOrThrow[Conf](contents) shouldBe Conf(
      "abc",
      true,
      BigInt("1234567890123456789012345678901234567890"),
      Set(4, 6, 8),
      List(10L, 10000L, 10000000L, 10000000000L),
      Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
      InnerConf(42, "def"))
  }

  it should "fail with a domain error when a non-string key is found" in {
    loadYaml[ConfigValue](resourcePath("non_string_keys.yaml")) should failWithType[NonStringKeyFound]
  }

  it should "fail with the correct config location filled when a YAML fails to be parsed" in {
    loadYaml[ConfigValue](resourcePath("illegal.yaml")) should failWith(CannotParse(
      "mapping values are not allowed here",
      Some(ConfigValueLocation(resourcePath("illegal.yaml").toUri.toURL, 3))))
  }

  it should "loadYamls from a multi-document YAML file" in {
    loadYamls[List[InnerConf]](resourcePath("multi.yaml")) shouldBe Right(List(
      InnerConf(1, "abc"),
      InnerConf(2, "def"),
      InnerConf(3, "ghi")))

    loadYamls[(InnerConf, Conf)](resourcePath("multi2.yaml")) shouldBe Right((
      InnerConf(1, "abc"),
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def"))))

    loadYamls[List[InnerConf]](Paths.get("nonexisting.yaml")) should failWithType[CannotReadFile]
  }

  it should "loadYamls from a string" in {
    val content = new String(Files.readAllBytes(resourcePath("multi2.yaml")))

    loadYamls[(InnerConf, Conf)](content) shouldBe Right((
      InnerConf(1, "abc"),
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def"))))
  }

  it should "loadYamlsOrThrow from a multi-document YAML file" in {
    loadYamlsOrThrow[List[InnerConf]](resourcePath("multi.yaml")) shouldBe List(
      InnerConf(1, "abc"),
      InnerConf(2, "def"),
      InnerConf(3, "ghi"))

    loadYamlsOrThrow[(InnerConf, Conf)](resourcePath("multi2.yaml")) shouldBe ((
      InnerConf(1, "abc"),
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def"))))

    a[ConfigReaderException[_]] should be thrownBy loadYamlsOrThrow[List[InnerConf]](Paths.get("nonexisting.yaml"))
  }

  it should "loadYamlsOrThrow from a string" in {
    val content = new String(Files.readAllBytes(resourcePath("multi2.yaml")))

    loadYamlsOrThrow[(InnerConf, Conf)](content) shouldBe ((
      InnerConf(1, "abc"),
      Conf(
        "abc",
        true,
        BigInt("1234567890123456789012345678901234567890"),
        Set(4, 6, 8),
        List(10L, 10000L, 10000000L, 10000000000L),
        Map("a" -> 1.5, "b" -> 2.5, "c" -> 3.5),
        InnerConf(42, "def"))))
  }
}
