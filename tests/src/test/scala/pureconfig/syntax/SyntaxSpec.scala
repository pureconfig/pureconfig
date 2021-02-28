package pureconfig.syntax

import scala.collection.immutable.{List, Map}

import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigReader
import pureconfig.error.ConfigReaderException

class SyntaxSpec extends AnyFlatSpec with Matchers {

  behavior of "pureconfig.syntax._"

  it should "be able to serialize a ConfigValue from a type with ConfigConvert using the toConfig method" in {
    Map("a" -> 1, "b" -> 2).toConfig shouldBe ConfigFactory.parseString("""{ "a": 1, "b": 2 }""").root()
  }

  it should "be able to load a ConfigValue to a type with ConfigConvert using the to method" in {
    val conf = ConfigFactory.parseString("""{ "a": [1, 2, 3, 4], "b": { "k1": "v1", "k2": "v2" } }""")
    conf.getList("a").to[List[Int]] shouldBe Right(List(1, 2, 3, 4))
    conf.getObject("b").to[Map[String, String]] shouldBe Right(Map("k1" -> "v1", "k2" -> "v2"))
  }

  it should "be able to load a Config to a type with ConfigConvert using the to method" in {
    val conf = ConfigFactory.parseString("""{ "a": [1, 2, 3, 4], "b": { "k1": "v1", "k2": "v2" } }""")
    case class Conf(a: List[Int], b: Map[String, String])
    implicit val confReader: ConfigReader[Conf] = ConfigReader.forProduct2("a", "b")(Conf.apply)
    conf.to[Conf] shouldBe Right(Conf(List(1, 2, 3, 4), Map("k1" -> "v1", "k2" -> "v2")))
  }

  // TODO this shouldn't be here
  it should "fail when trying to convert to basic types from an empty string" in {
    val conf = ConfigFactory.parseString("""{ v: "" }""")
    conf.getValue("v").to[Boolean].isLeft shouldBe true
    conf.getValue("v").to[Double].isLeft shouldBe true
    conf.getValue("v").to[Float].isLeft shouldBe true
    conf.getValue("v").to[Int].isLeft shouldBe true
    conf.getValue("v").to[Long].isLeft shouldBe true
    conf.getValue("v").to[Short].isLeft shouldBe true
  }

  it should "fail with Exception when trying to convert to basic types from an empty string" in {
    val conf = ConfigFactory.parseString("""{ v: "" }""")

    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Boolean]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Double]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Float]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Int]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Long]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Short]
  }

  it should "pass when trying to convert to basic types with pureconfig.syntax toOrThrow" in {
    val conf = ConfigFactory.parseString("""{ b: true, d: 2.2, f: 3.3, i: 2, l: 2, s: 2, cs: "Cheese"}""")

    conf.getValue("b").toOrThrow[Boolean] shouldBe true
    conf.getValue("d").toOrThrow[Double] shouldBe 2.2
    conf.getValue("f").toOrThrow[Float] shouldBe 3.3f
    conf.getValue("i").toOrThrow[Int] shouldBe 2
    conf.getValue("l").toOrThrow[Long] shouldBe 2L
    conf.getValue("s").toOrThrow[Short] shouldBe 2.toShort
    conf.getValue("cs").toOrThrow[String] shouldBe "Cheese"

  }
}
