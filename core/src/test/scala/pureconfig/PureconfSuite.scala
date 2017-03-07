/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.io.PrintWriter
import java.net.{ URI, URL }
import java.nio.file.{ Files, Path, Paths }
import java.time._
import java.util.UUID
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._
import scala.collection.immutable._
import scala.concurrent.duration.{ Duration, FiniteDuration }
import com.typesafe.config.{ ConfigFactory, Config => TypesafeConfig, _ }
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalacheck.Arbitrary
import org.scalacheck.Gen.uuid
import org.scalacheck.Shapeless._
import org.scalatest._
import org.scalatest.prop.PropertyChecks
import pureconfig.ConfigConvert.{ catchReadError, fromStringConvert, fromStringReader }
import pureconfig.error.{ ConfigReaderException, _ }

/**
 * @author Mario Pastorelli
 */
object PureconfSuite {
  def withTempFile(f: Path => Unit): Unit = {
    val configFile = Files.createTempFile("pureconftest", ".property")
    f(configFile)
    Files.delete(configFile)
  }

  def fileList(names: String*): Seq[java.io.File] = {
    names.map(new java.io.File(_)).toVector
  }
}

import pureconfig.PureconfSuite._

class PureconfSuite extends FlatSpec with Matchers with OptionValues with EitherValues with PropertyChecks {

  // checks if saving and loading a configuration from file returns the configuration itself
  def saveAndLoadIsIdentity[C](config: C)(implicit configConvert: ConfigConvert[C]): Unit = {
    withTempFile { configFile =>
      saveConfigAsPropertyFile(config, configFile, overrideOutputPath = true)
      loadConfig[C](configFile) shouldEqual Right(config)
    }
  }

  // a simple "flat" configuration
  case class FlatConfig(b: Boolean, d: Double, f: Float, i: Int, l: Long, s: String, o: Option[String])
  implicitly[Arbitrary[FlatConfig]]

  "pureconfig" should s"be able to save and load ${classOf[FlatConfig]}" in forAll {
    (expectedConfig: FlatConfig) =>
      withTempFile { configFile =>
        saveConfigAsPropertyFile(expectedConfig, configFile, overrideOutputPath = true)
        val config = loadConfig[FlatConfig](configFile)

        config should be(Right(expectedConfig))
      }
  }

  it should "be able to serialize a ConfigValue from a type with ConfigConvert using the toConfig method" in {
    import pureconfig.syntax._

    Map("a" -> 1, "b" -> 2).toConfig shouldBe ConfigFactory.parseString("""{ "a": 1, "b": 2 }""").root()
  }

  it should "be able to load a ConfigValue to a type with ConfigConvert using the to method" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ "a": [1, 2, 3, 4], "b": { "k1": "v1", "k2": "v2" } }""")
    conf.getList("a").to[List[Int]] shouldBe Right(List(1, 2, 3, 4))
    conf.getObject("b").to[Map[String, String]] shouldBe Right(Map("k1" -> "v1", "k2" -> "v2"))
  }

  it should "be able to load a Config to a type with ConfigConvert using the to method" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ "a": [1, 2, 3, 4], "b": { "k1": "v1", "k2": "v2" } }""")
    case class Conf(a: List[Int], b: Map[String, String])
    conf.to[Conf] shouldBe Right(Conf(List(1, 2, 3, 4), Map("k1" -> "v1", "k2" -> "v2")))
  }

  it should s"be able to override locally all of the ConfigConvert instances used to parse ${classOf[FlatConfig]}" in {
    implicit val readBoolean = fromStringReader[Boolean](catchReadError(_ != "0"))
    implicit val readDouble = fromStringReader[Double](catchReadError(_.toDouble * -1))
    implicit val readFloat = fromStringReader[Float](catchReadError(_.toFloat * -1))
    implicit val readInt = fromStringReader[Int](catchReadError(_.toInt * -1))
    implicit val readLong = fromStringReader[Long](catchReadError(_.toLong * -1))
    implicit val readString = fromStringReader[String](catchReadError(_.toUpperCase))
    val config = loadConfig[FlatConfig](ConfigValueFactory.fromMap(Map(
      "b" -> 0,
      "d" -> 234.234,
      "f" -> 34.34,
      "i" -> 56,
      "l" -> -88,
      "s" -> "qwerTy").asJava).toConfig)

    config.right.value shouldBe FlatConfig(false, -234.234d, -34.34f, -56, 88L, "QWERTY", None)
  }

  it should "fail when trying to convert to basic types from an empty string" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ v: "" }""")
    conf.getValue("v").to[Boolean].isLeft shouldBe true
    conf.getValue("v").to[Double].isLeft shouldBe true
    conf.getValue("v").to[Float].isLeft shouldBe true
    conf.getValue("v").to[Int].isLeft shouldBe true
    conf.getValue("v").to[Long].isLeft shouldBe true
    conf.getValue("v").to[Short].isLeft shouldBe true
  }

  it should "fail with Exception when trying to convert to basic types from an empty string" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ v: "" }""")

    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Boolean]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Double]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Float]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Int]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Long]
    a[ConfigReaderException[_]] should be thrownBy conf.getValue("v").toOrThrow[Short]
  }

  it should "pass when trying to convert to basic types with pureconfig.syntax toOrThrow" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ b: true, d: 2.2, f: 3.3, i: 2, l: 2, s: 2, cs: "Cheese"}""")

    conf.getValue("b").toOrThrow[Boolean] shouldBe true
    conf.getValue("d").toOrThrow[Double] shouldBe 2.2
    conf.getValue("f").toOrThrow[Float] shouldBe 3.3f
    conf.getValue("i").toOrThrow[Int] shouldBe 2
    conf.getValue("l").toOrThrow[Long] shouldBe 2L
    conf.getValue("s").toOrThrow[Short] shouldBe 2.toShort
    conf.getValue("cs").toOrThrow[String] shouldBe "Cheese"

  }

  case class ConfigWithDouble(v: Double)

  it should "be able to load a Double from a percentage" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ v: 52% }""")
    conf.to[ConfigWithDouble] shouldBe Right(ConfigWithDouble(0.52))
  }

  it should "be able to load Typesafe Config types directly" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{
      list = [1, 2, 3]
      v1 = 4
      v2 = "str"
      m {
        k1 {
          v1 = 3
          v2 = 4
        }
        k2 {
          v1 = 10
          v3 = "str"
        }
        k3 {
          v1 = 5
          v4 {
            v5 = 6
          }
        }
      }
    }""")

    conf.getValue("list").to[ConfigList] shouldBe Right(ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))
    conf.getValue("list").to[ConfigValue] shouldBe Right(ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))
    conf.getValue("v1").to[ConfigValue] shouldBe Right(ConfigValueFactory.fromAnyRef(4))
    conf.getValue("v2").to[ConfigValue] shouldBe Right(ConfigValueFactory.fromAnyRef("str"))
    conf.getValue("m.k1").to[ConfigObject] shouldBe Right(ConfigFactory.parseString("""{
      v1 = 3
      v2 = 4
    }""").root())
    conf.getConfig("m").to[Map[String, TypesafeConfig]] shouldBe Right(Map(
      "k1" -> ConfigFactory.parseString("""{
        v1 = 3
        v2 = 4
      }"""),
      "k2" -> ConfigFactory.parseString("""{
        v1 = 10
        v3 = "str"
      }"""),
      "k3" -> ConfigFactory.parseString("""{
        v1 = 5
        v4 {
          v5 = 6
        }
      }""")))
  }

  // load HOCON-style lists
  case class ConfigWithHoconList(xs: List[Int])

  it should s"be able to load ${classOf[ConfigWithHoconList]}" in {
    withTempFile { configFile =>

      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.write("xs: [1, 2, 3]")
      writer.close()

      val config = loadConfig[ConfigWithHoconList](configFile)

      config should be(Right(ConfigWithHoconList(xs = List(1, 2, 3))))
    }
  }

  // a slightly more complex configuration
  implicit val dateConfigConvert = fromStringConvert[DateTime](
    catchReadError(ISODateTimeFormat.dateTime().parseDateTime),
    t => ISODateTimeFormat.dateTime().print(t))

  case class Config(d: DateTime, l: List[Int], s: Set[Int], subConfig: FlatConfig)

  it should s"be able to save and load ${classOf[Config]}" in {
    withTempFile { configFile =>
      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")))
      saveConfigAsPropertyFile(expectedConfig, configFile, overrideOutputPath = true)
      val config = loadConfig[Config](configFile)

      config should be(Right(expectedConfig))
    }
  }

  // the same configuration but with custom namespace
  case class Config2(config: Config)

  it should s"be able to save ${classOf[Config2]} and load ${classOf[Config]} when namespace is set to config" in {
    withTempFile { configFile =>
      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")))
      val configToSave = Config2(expectedConfig)
      saveConfigAsPropertyFile(configToSave, configFile, overrideOutputPath = true)
      val config = loadConfig[Config](configFile, "config")

      config should be(Right(expectedConfig))
    }
  }

  sealed trait AnimalConfig
  case class DogConfig(age: Int) extends AnimalConfig
  case class CatConfig(age: Int) extends AnimalConfig
  case class BirdConfig(canFly: Boolean) extends AnimalConfig

  it should s"be able to save and load ${classOf[AnimalConfig]}" in {
    List(DogConfig(12), CatConfig(3), BirdConfig(true)).foreach { expectedConfig =>
      saveAndLoadIsIdentity[AnimalConfig](expectedConfig)
    }
  }

  it should s"read and write disambiguation information on sealed families by default" in {
    withTempFile { configFile =>
      val conf = ConfigFactory.parseString("{ type = dogconfig, age = 2 }")
      loadConfig[AnimalConfig](conf) should be(Right(DogConfig(2)))

      saveConfigAsPropertyFile[AnimalConfig](DogConfig(2), configFile, overrideOutputPath = true)
      loadConfig[TypesafeConfig](configFile).right.map(_.getString("type")) should be(Right("dogconfig"))
    }
  }

  it should s"allow using different strategies for disambiguating between options in a sealed family" in {
    withTempFile { configFile =>
      implicit val hint = new FieldCoproductHint[AnimalConfig]("which-animal") {
        override def fieldValue(name: String) = name.dropRight("Config".length)
      }

      val conf = ConfigFactory.parseString("{ which-animal = Dog, age = 2 }")
      loadConfig[AnimalConfig](conf) should be(Right(DogConfig(2)))

      saveConfigAsPropertyFile[AnimalConfig](DogConfig(2), configFile, overrideOutputPath = true)
      loadConfig[TypesafeConfig](configFile).right.map(_.getString("which-animal")) should be(Right("Dog"))
    }

    withTempFile { configFile =>
      implicit val hint = new FirstSuccessCoproductHint[AnimalConfig]

      val conf = ConfigFactory.parseString("{ can-fly = true }")
      loadConfig[AnimalConfig](conf) should be(Right(BirdConfig(true)))

      saveConfigAsPropertyFile[AnimalConfig](DogConfig(2), configFile, overrideOutputPath = true)
      loadConfig[TypesafeConfig](configFile).right.map(_.hasPath("type")) should be(Right(false))
    }
  }

  it should "throw an exception if a coproduct option has a field with the same key as the hint field" in {
    implicit val hint = new FieldCoproductHint[AnimalConfig]("age")
    val cc = implicitly[ConfigConvert[AnimalConfig]]
    a[ConfigReaderException[_]] should be thrownBy cc.to(DogConfig(2))
  }

  it should "return a Failure with a proper exception if the hint field in a coproduct is missing" in {
    val conf = ConfigFactory.parseString("{ can-fly = true }")
    val failures = loadConfig[AnimalConfig](conf).left.value.toList
    failures should have size 1
    failures.head shouldBe a[KeyNotFound]
  }

  it should "return a Failure with a proper exception when a coproduct config is missing" in {
    case class AnimalCage(animal: AnimalConfig)
    val failures = loadConfig[AnimalCage](ConfigFactory.empty()).left.value.toList
    failures should have size 1
    failures.head shouldBe a[KeyNotFound]
  }

  // a realistic example of configuration: common available Spark properties
  case class DriverConf(cores: Int, maxResultSize: String, memory: String)
  case class ExecutorConf(memory: String, extraJavaOptions: String)
  case class SparkAppConf(name: String)
  case class SparkLocalConf(dir: String)
  case class SparkNetwork(timeout: FiniteDuration)
  case class SparkConf(master: String, app: SparkAppConf, local: SparkLocalConf, driver: DriverConf, executor: ExecutorConf, extraListeners: Seq[String], network: SparkNetwork)
  case class SparkRootConf(spark: SparkConf)

  it should s"be able to save and load ${classOf[SparkRootConf]}" in {
    withTempFile { configFile =>

      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("""spark.executor.extraJavaOptions=""""")
      writer.println("""spark.driver.maxResultSize="2g"""")
      writer.println("""spark.extraListeners=[]""")
      writer.println("""spark.app.name="myApp"""")
      writer.println("""spark.driver.memory="1g"""")
      writer.println("""spark.driver.cores="10"""")
      writer.println("""spark.master="local[*]"""")
      writer.println("""spark.executor.memory="2g"""")
      writer.println("""spark.local.dir="/tmp/"""")
      writer.println("""spark.network.timeout=45s""")
      // unused configuration
      writer.println("""akka.loggers = ["akka.event.Logging$DefaultLogger"]""")
      writer.close()

      implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
      val configOrError = loadConfig[SparkRootConf](configFile)

      val config = configOrError match {
        case Left(f) => fail(f.toString)
        case Right(c) => c
      }

      config.spark.executor.extraJavaOptions should be("")
      config.spark.driver.maxResultSize should be("2g")
      config.spark.extraListeners should be(Seq.empty[String])
      config.spark.app.name should be("myApp")
      config.spark.driver.memory should be("1g")
      config.spark.driver.cores should be(10)
      config.spark.master should be("local[*]")
      config.spark.executor.memory should be("2g")
      config.spark.local.dir should be("/tmp/")
      config.spark.network.timeout should be(FiniteDuration(45, TimeUnit.SECONDS))
    }
  }

  case class MapConf(conf: Map[String, Int])

  it should s"be able to save and load Map like fields" in {
    withTempFile { configFile =>
      val expected = MapConf(Map("a" -> 1, "b" -> 2, "c" -> 3))
      saveConfigAsPropertyFile(expected, configFile, overrideOutputPath = true)
      val result = loadConfig[MapConf](configFile)

      result shouldEqual (Right(expected))
    }
  }

  it should s"not be able to load invalid Maps" in {
    // invalid value
    withTempFile { configFile =>
      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("conf.a=foo")
      writer.close()

      val result = loadConfig[MapConf](configFile)

      result.isLeft shouldEqual true
    }

    // invalid key because it contains the namespace separator
    withTempFile { configFile =>
      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("conf.a.b=1")
      writer.close()

      val result = loadConfig[MapConf](configFile)

      result.isLeft shouldEqual true
    }
  }

  it should s"properly load maps from the provided namespace" in {
    withTempFile { configFile =>
      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("conf.a=1")
      writer.println("conf1.a=1")
      writer.close()

      val result = loadConfig[MapConf](configFile)
      val expected = MapConf(Map("a" -> 1))

      result shouldEqual Right(expected)
    }
  }

  it should s"properly load maps from the base namespace" in {
    val cf = ConfigFactory.parseString("""{
      a = 1
      b = 2
      c = 3
    }""")

    val result = loadConfig[Map[String, Int]](cf)
    val expected = Map("a" -> 1, "b" -> 2, "c" -> 3)

    result shouldEqual Right(expected)
  }

  // traversable of complex types

  case class Foo(i: Int)
  case class ConfWithListOfPair(list: List[(String, Int)])

  it should s"be able to save and load configurations containing immutable.List" in {
    saveAndLoadIsIdentity(ConfWithListOfPair(List("foo" -> 1, "bar" -> 2)))
  }

  case class ConfWithListOfFoo(list: List[Foo])

  it should s"be able to load a list of Foo from a HOCON file" in {
    val conf = ConfigFactory.parseString("""{
      list = [{ i = 1 }, { i = 2 }, { i = 3 }]
    }""")
    val expected = ConfWithListOfFoo(List(Foo(1), Foo(2), Foo(3)))
    loadConfig[ConfWithListOfFoo](conf) shouldBe Right(expected)
  }

  it should "be able to load a set of Ints from an object with numeric keys" in {
    val conf = ConfigFactory.parseString("""
    pure.conf: {
      intSet.0: 1
      intSet.1: 2
    }""")

    val expected = Set(1, 2)
    loadConfig[Set[Int]](conf, "pure.conf.intSet") shouldBe Right(expected)
  }

  it should "be able to load a list of Ints from an object with numeric keys (in correct order)" in {
    val conf = ConfigFactory.parseString("""
    pure.conf: {
      intList.2: 1
      intList.0: 2
      intList.1: 3
    }""")

    val expected = List(2, 3, 1)
    loadConfig[List[Int]](conf, "pure.conf.intList") shouldBe Right(expected)
  }

  it should "be able to load a list of Ints from an object with numeric keys in correct order when one element is missing" in {
    val conf = ConfigFactory.parseString("""
    pure.conf: {
      intList.2: 3
      intList.0: 1
    }""")

    val expected = List(1, 3)
    loadConfig[List[Int]](conf, "pure.conf.intList") shouldBe Right(expected)
  }

  case class ConfWithStreamOfFoo(stream: Stream[Foo])

  it should s"be able to save and load configurations containing immutable.Stream" in {
    saveAndLoadIsIdentity(ConfWithStreamOfFoo(Stream(Foo(1), Foo(2))))
  }

  case class Bar(foo: Foo)
  case class ConfWithSetOfBar(set: Set[Bar])

  it should s"be able to save and load configurations containing immutable.Set" in {
    saveAndLoadIsIdentity(ConfWithSetOfBar(Set(Bar(Foo(1)), Bar(Foo(2)))))
  }

  case class ConfWithQueueOfFoo(queue: Queue[Foo])

  it should s"be able to save and load configurations containing immutable.Queue" in {
    saveAndLoadIsIdentity(ConfWithQueueOfFoo(Queue(Foo(1), Foo(2))))
  }

  case class ConfWithHashSetOfFoo(hashSet: HashSet[Foo])

  it should s"be able to save and load configurations containing immutable.HashSet" in {
    saveAndLoadIsIdentity(ConfWithHashSetOfFoo(HashSet(Foo(1))))
  }

  case class ConfWithListSetOfFoo(listSet: ListSet[Foo])

  it should s"be able to save and load configurations containing immutable.ListSet" in {
    saveAndLoadIsIdentity(ConfWithListSetOfFoo(ListSet(Foo(2))))
  }

  case class ConfWithVectorOfFoo(vector: Vector[Foo])

  it should s"be able to save and load configurations containing immutable.Vector" in {
    saveAndLoadIsIdentity(ConfWithVectorOfFoo(Vector(Foo(1))))
  }

  // map of complex types

  case class ConfWithMapOfFoo(map: Map[String, Foo])

  it should s"be able to save and load configurations containing map of Foo" in {
    saveAndLoadIsIdentity(ConfWithMapOfFoo(Map("a" -> Foo(1), "b" -> Foo(2))))
  }

  case class ConfWithFoo(foo: Foo)

  it should "be able to use a local ConfigConvert without getting an ImplicitResolutionFailure error" in {
    implicit val custom: ConfigConvert[Foo] = fromStringConvert(catchReadError(s => Foo(s.toInt)), _.i.toString)
    saveAndLoadIsIdentity(ConfWithFoo(Foo(100)))
  }

  case class ConfWithInt(i: Int)

  it should "be able to use a local ConfigConvert instead of the ones in ConfigConvert companion object" in {
    implicit val readInt = fromStringReader[Int](catchReadError(s => (s.toInt.abs)))
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "-100").asJava).toConfig)(ConfigConvert[ConfWithInt]).right.value shouldBe ConfWithInt(100)
  }

  case class ConfWithDuration(i: Duration)

  it should "be able to supersede the default Duration ConfigConvert with a locally defined ConfigConvert from fromString" in {
    val expected = Duration(110, TimeUnit.DAYS)
    implicit val readDurationBadly = fromStringReader[Duration](catchReadError(_ => expected))
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "23 s").asJava).toConfig)(ConfigConvert[ConfWithDuration]).right.value shouldBe ConfWithDuration(expected)
  }

  case class ConfWithInstant(instant: Instant)

  it should "be able to read a config with an Instant" in {
    val expected = Instant.now()
    val config = ConfigFactory.parseString(s"""{ "instant":"${expected.toString}" }""")
    loadConfig[ConfWithInstant](config).right.value shouldEqual ConfWithInstant(expected)
  }

  case class ConfWithZoneOffset(offset: ZoneOffset)

  it should "be able to read a config with a ZoneOffset" in {
    val expected = ZoneOffset.ofHours(10)
    val config = ConfigFactory.parseString(s"""{ "offset":"${expected.toString}" }""")
    loadConfig[ConfWithZoneOffset](config).right.value shouldBe ConfWithZoneOffset(expected)
  }

  case class ConfWithZoneId(zoneId: ZoneId)

  it should "be able to read a config with a ZoneId" in {
    val expected = ZoneId.systemDefault()
    val config = ConfigFactory.parseString(s"""{ "zone-id":"${expected.toString}" }""")
    loadConfig[ConfWithZoneId](config).right.value shouldBe ConfWithZoneId(expected)
  }

  case class ConfWithPeriod(period: Period)

  it should "be able to read a config with a Period" in {
    val expected = Period.of(2016, 1, 1)
    val config = ConfigFactory.parseString(s"""{ "period":"${expected.toString}" }""")
    loadConfig[ConfWithPeriod](config).right.value shouldBe ConfWithPeriod(expected)
  }

  case class ConfWithYear(year: Year)

  it should "be able to read a config with a Year" in {
    val expected = Year.now()
    val config = ConfigFactory.parseString(s"""{ "year":"${expected.toString}" }""")
    loadConfig[ConfWithYear](config).right.value shouldBe ConfWithYear(expected)
  }

  it should "be able to supersede the default Duration ConfigConvert with a locally defined ConfigConvert" in {
    val expected = Duration(220, TimeUnit.DAYS)
    implicit val readDurationBadly = new ConfigConvert[Duration] {
      override def from(config: ConfigValue): Either[ConfigReaderFailures, Duration] = Right(expected)
      override def to(t: Duration): ConfigValue = throw new Exception("Not Implemented")
    }
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "42 h").asJava).toConfig)(ConfigConvert[ConfWithDuration]).right.value shouldBe ConfWithDuration(expected)
  }

  it should "custom ConfigConvert should not cause implicit resolution failure and should be used" in {
    implicit val custom: ConfigConvert[Foo] = new ConfigConvert[Foo] {
      def from(config: ConfigValue): Either[ConfigReaderFailures, Foo] = {
        val s = config.asInstanceOf[ConfigObject].get("i").render()
        catchReadError(s => Foo(s.toInt + 1))(implicitly)(s)(None).left.map(ConfigReaderFailures.apply)
      }
      def to(foo: Foo): ConfigValue =
        ConfigValueFactory.fromMap(Map("i" -> foo.i).asJava)
    }
    loadConfig(ConfigFactory.parseString("foo.i = -100"))(ConfigConvert[ConfWithFoo]).right.value shouldBe ConfWithFoo(Foo(-99))
  }

  case class ConfWithURL(url: URL)

  it should "be able to read a config with a URL" in {
    val expected = "http://host/path?with=query&param"
    val config = loadConfig[ConfWithURL](ConfigValueFactory.fromMap(Map("url" -> expected).asJava).toConfig)
    config.right.value.url shouldBe new URL(expected)
  }

  it should "round trip a URL" in {
    saveAndLoadIsIdentity(ConfWithURL(new URL("https://you/spin?me&right=round")))
  }

  it should "allow a custom ConfigConvert[URL] to override our definition" in {
    val expected = "http://bad/horse/will?make=you&his=mare"
    implicit val readURLBadly = fromStringReader[URL](catchReadError(_ => new URL(expected)))
    val config = loadConfig[ConfWithURL](ConfigValueFactory.fromMap(Map("url" -> "https://ignored/url").asJava).toConfig)
    config.right.value.url shouldBe new URL(expected)
  }

  case class ConfWithUUID(uuid: UUID)

  it should "be able to read a config with a UUID" in {
    val expected = "d25aed6a-ef6d-4c10-954c-02edc949aef1"
    val config = loadConfig[ConfWithUUID](ConfigValueFactory.fromMap(Map("uuid" -> expected).asJava).toConfig)
    config.right.value.uuid shouldBe UUID.fromString(expected)
  }

  it should "round trip a UUID" in forAll(uuid) { (uuid: UUID) =>
    saveAndLoadIsIdentity(ConfWithUUID(uuid))
  }

  it should "allow a custom ConfigConvert[UUID] to override our definition" in {
    val expected = "bcd787fe-f510-4f84-9e64-f843afd19c60"
    implicit val readUUIDBadly = fromStringReader[UUID](catchReadError(_ => UUID.fromString(expected)))
    val config = loadConfig[ConfWithUUID](ConfigValueFactory.fromMap(Map("uuid" -> "ignored").asJava).toConfig)
    config.right.value.uuid shouldBe UUID.fromString(expected)
  }

  case class ConfWithPath(myPath: Path)

  it should "be able to read a config with a Path" in {
    val expected = "/tmp/foo.bar"
    val config = loadConfig[ConfWithPath](ConfigValueFactory.fromMap(Map("my-path" -> expected).asJava).toConfig)
    config.right.value.myPath shouldBe Paths.get(expected)
  }

  it should "round trip a Path" in {
    saveAndLoadIsIdentity(ConfWithPath(Paths.get("/tmp/foo.bar")))
  }

  it should "allow a custom ConfigConvert[Path] to override our definition" in {
    val expected = "c:\\this\\is\\a\\custom\\path"
    implicit val readPathBadly = fromStringReader[Path](_ => _ => Right(Paths.get(expected)))
    val config = loadConfig[ConfWithPath](ConfigValueFactory.fromMap(Map("my-path" -> "/this/is/ignored").asJava).toConfig)
    config.right.value.myPath shouldBe Paths.get(expected)
  }

  case class ConfWithURI(uri: URI)

  it should "be able to read a config with a URI" in {
    val expected = "http://host/path?with=query&param"
    val config = loadConfig[ConfWithURI](ConfigValueFactory.fromMap(Map("uri" -> expected).asJava).toConfig)
    config.right.value.uri shouldBe new URI(expected)
  }

  it should "round trip a URI" in {
    saveAndLoadIsIdentity(ConfWithURI(new URI("https://you/spin?me&right=round")))
  }

  it should "allow a custom ConfigConvert[URI] to override our definition" in {
    val expected = "http://bad/horse/will?make=you&his=mare"
    implicit val readURLBadly = fromStringReader[URI](_ => _ => Right(new URI(expected)))
    val config = loadConfig[ConfWithURI](ConfigValueFactory.fromMap(Map("uri" -> "https://ignored/url").asJava).toConfig)
    config.right.value.uri shouldBe new URI(expected)
  }

  case class ConfWithCamelCaseInner(thisIsAnInt: Int, thisIsAnotherInt: Int)
  case class ConfWithCamelCase(camelCaseInt: Int, camelCaseString: String, camelCaseConf: ConfWithCamelCaseInner)

  it should "read kebab case config keys to camel case fields by default" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{
      camel-case-int = 1
      camel-case-string = "bar"
      camel-case-conf {
        this-is-an-int = 3
        this-is-another-int = 10
      }
    }""")

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "allow customizing the field mapping through a product hint" in {
    val conf = ConfigFactory.parseString("""{
      A = 2
      B = "two"
    }""")

    case class SampleConf(a: Int, b: String)
    loadConfig[SampleConf](conf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("a", None), KeyNotFound("b", None))

    implicit val productHint = ProductHint[SampleConf](ConfigFieldMapping(_.toUpperCase))

    loadConfig[SampleConf](conf) shouldBe Right(SampleConf(2, "two"))
  }

  it should "allow customizing the field mapping with different naming conventions" in {
    import pureconfig.syntax._

    {
      implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

      val conf = ConfigFactory.parseString("""{
        camelCaseInt = 1
        camelCaseString = "bar"
        camelCaseConf {
          thisIsAnInt = 3
          thisIsAnotherInt = 10
        }
      }""")

      conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
    }

    {
      implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, PascalCase))

      val conf = ConfigFactory.parseString(
        """{
          CamelCaseInt = 1
          CamelCaseString = "bar"
          CamelCaseConf {
            ThisIsAnInt = 3
            ThisIsAnotherInt = 10
          }
        }""")

      conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
    }
  }

  it should "allow customizing the field mapping only for specific types" in {
    import pureconfig.syntax._

    implicit val productHint = ProductHint[ConfWithCamelCase](ConfigFieldMapping(CamelCase, CamelCase))

    val conf = ConfigFactory.parseString("""{
      camelCaseInt = 1
      camelCaseString = "bar"
      camelCaseConf {
        this-is-an-int = 3
        this-is-another-int = 10
      }
    }""")

    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  }

  it should "disallow unknown keys if specified through a product hint" in {
    import pureconfig.syntax._

    case class Conf1(a: Int)
    case class Conf2(a: Int)

    implicit val productHint = ProductHint[Conf2](allowUnknownKeys = false)

    val conf = ConfigFactory.parseString("""{
      conf {
        a = 1
        b = 2
      }
    }""")

    conf.getConfig("conf").to[Conf1] shouldBe Right(Conf1(1))
    val failures = conf.getConfig("conf").to[Conf2].left.value.toList
    failures should have size 1
    failures.head shouldBe a[UnknownKey]
  }

  val expectedValueForResolveFilesPriority2 = FlatConfig(
    false,
    0.001d,
    99.99f,
    42,
    1234567890123456L,
    "cheese",
    Some("thing"))

  "loadConfigFromFiles" should "load a complete configuration from a single file" in {
    val files = fileList(
      "core/src/test/resources/conf/loadConfigFromFiles/priority2.conf")
    loadConfigFromFiles[FlatConfig](files).right.get shouldBe expectedValueForResolveFilesPriority2
  }

  "loadConfigWithFallBack" should "fallback if no config keys are found" in {
    val priority1Conf = ConfigFactory.load("conf/loadConfigFromFiles/priority1.conf")
    val actual = loadConfigWithFallback[FlatConfig](priority1Conf)
    actual.right.get shouldBe FlatConfig(
      true,
      0.0d,
      0.99f,
      0,
      1L,
      "default",
      None)
  }

  it should "fill in missing values from the lower priority files" in {
    val files = fileList(
      "core/src/test/resources/conf/loadConfigFromFiles/priority1.conf",
      "core/src/test/resources/conf/loadConfigFromFiles/priority2.conf")
    val actual = loadConfigFromFiles[FlatConfig](files)
    actual.right.get shouldBe FlatConfig(
      true,
      0.001d,
      0.99f,
      42,
      1L,
      "cheese",
      None // Notice that a Some in a lower priority file does not override a None.
    )
  }

  it should "complain if the configuration is incomplete" in {
    val files = fileList(
      "core/src/test/resources/conf/loadConfigFromFiles/priority1.conf")
    val actual = loadConfigFromFiles[FlatConfig](files)
    actual.isLeft shouldBe true
  }

  it should "silently ignore files which can't be read" in {
    val files = fileList(
      "core/src/test/resources/conf/loadConfigFromFiles/this.is.not.a.conf",
      "core/src/test/resources/conf/loadConfigFromFiles/priority2.conf")
    loadConfigFromFiles[FlatConfig](files).right.value shouldBe expectedValueForResolveFilesPriority2
  }

  it should "complain if the list of files is empty" in {
    val files = fileList()
    loadConfigFromFiles[FlatConfig](files) shouldBe a[Left[_, _]]
  }

  case class FooBar(foo: Foo, bar: Bar)
  case class ConfWithConfigObject(conf: ConfigObject)
  case class ConfWithConfigList(conf: ConfigList)

  it should s"return a ${classOf[KeyNotFound]} when a key is not in the configuration" in {
    val emptyConf = ConfigFactory.empty()
    loadConfig[Foo](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("i", None))
    val conf = ConfigFactory.parseMap(Map("namespace.foo" -> 1).asJava)
    loadConfig[Foo](conf, "namespace").left.value.toList should contain theSameElementsAs Seq(KeyNotFound("namespace.i", None))
    loadConfig[ConfWithMapOfFoo](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("map", None))
    loadConfig[ConfWithListOfFoo](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("list", None))
    loadConfig[ConfWithConfigObject](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("conf", None))
    loadConfig[ConfWithConfigList](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("conf", None))
    loadConfig[ConfWithDuration](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("i", None))
    loadConfig[SparkNetwork](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("timeout", None))

    case class InnerConf(v: Int)
    case class EnclosingConf(conf: InnerConf)

    implicit val conv = new ConfigConvert[InnerConf] {
      def from(cv: ConfigValue) = Right(InnerConf(42))
      def to(conf: InnerConf) = ConfigFactory.parseString(s"{ v: ${conf.v} }").root()
    }

    loadConfig[EnclosingConf](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("conf", None))
  }

  it should "allow custom ConfigConverts to handle missing keys" in {
    case class Conf(a: Int, b: Int)
    val conf = ConfigFactory.parseString("""{ a: 1 }""")
    loadConfig[Conf](conf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("b", None))

    implicit val defaultInt = new ConfigConvert[Int] with AllowMissingKey {
      def from(v: ConfigValue) =
        if (v == null) Right(42) else {
          val s = v.render(ConfigRenderOptions.concise)
          catchReadError(_.toInt)(implicitly)(s)(None).left.map(ConfigReaderFailures.apply)
        }
      def to(v: Int) = ???
    }
    loadConfig[Conf](conf).right.value shouldBe Conf(1, 42)
  }

  it should s"return a ${classOf[WrongType]} when a value has a wrong type" in {
    val conf = ConfigFactory.parseMap(Map("foo.i" -> 1, "bar.foo" -> "").asJava)
    val failures = loadConfig[FooBar](conf).left.value.toList
    failures should have size 1
    failures.head shouldBe a[WrongType]

    val conf1 = ConfigFactory.parseMap(Map("ns.foo.i" -> 1, "ns.bar.foo" -> "").asJava)
    val failures1 = loadConfig[FooBar](conf1, "ns").left.value.toList
    failures1 should have size 1
    failures1.head shouldBe a[WrongType]

    val conf2 = ConfigFactory.parseString("""{ map: [{ i: 1 }, { i: 2 }, { i: 3 }] }""")
    val failures2 = loadConfig[ConfWithMapOfFoo](conf2).left.value.toList
    failures2 should have size 1
    failures2.head shouldBe a[WrongType]

    val conf3 = ConfigFactory.parseString("""{ conf: [{ i: 1 }, { i: 2 }, { i: 3 }] }""")
    val failures3 = loadConfig[ConfWithConfigObject](conf3).left.value.toList
    failures3 should have size 1
    failures3.head shouldBe a[WrongType]

    val conf4 = ConfigFactory.parseString("""{ conf: { a: 1, b: 2 }}""")
    val failures4 = loadConfig[ConfWithConfigList](conf4).left.value.toList
    failures4 should have size 1
    failures4.head shouldBe a[WrongType]
  }

  it should "consider default arguments by default" in {
    case class InnerConf(e: Int, g: Int)
    case class Conf(a: Int, b: String = "default", c: Int = 42, d: InnerConf = InnerConf(43, 44))

    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava)
    loadConfig[Conf](conf1).right.value shouldBe Conf(2, "default", 42, InnerConf(43, 44))

    val conf2 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50).asJava)
    loadConfig[Conf](conf2).right.value shouldBe Conf(2, "default", 50, InnerConf(43, 44))

    val conf3 = ConfigFactory.parseMap(Map("c" -> 50).asJava)
    loadConfig[Conf](conf3).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("a", None))

    val conf4 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5).asJava)
    loadConfig[Conf](conf4).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("d.g", None))

    val conf5 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5, "d.g" -> 6).asJava)
    loadConfig[Conf](conf5).right.value shouldBe Conf(2, "default", 42, InnerConf(5, 6))

    val conf6 = ConfigFactory.parseMap(Map("a" -> 2, "d" -> "notAnInnerConf").asJava)
    val failures = loadConfig[Conf](conf6).left.value.toList
    failures should have size 1
    failures.head shouldBe a[WrongType]
  }

  it should "not use default arguments if specified through a product hint" in {
    case class InnerConf(e: Int, g: Int)
    case class Conf(a: Int, b: String = "default", c: Int = 42, d: InnerConf = InnerConf(43, 44))

    implicit val productHint = ProductHint[Conf](useDefaultArgs = false)

    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava)
    loadConfig[Conf](conf1).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("b", None), KeyNotFound("c", None), KeyNotFound("d", None))
  }

  "Converting from an empty string to a double" should "complain about an empty string" in {
    val conf = ConfigFactory.parseMap(Map("v" -> "").asJava)
    loadConfig[ConfigWithDouble](conf) shouldBe a[Left[_, _]]
  }

  "Converting from a wrong list object that has non-numeric keys" should "complain about having the wrong list syntax" in {
    val conf = ConfigFactory.parseString("""
    pure.conf: {
      intSet.0: 1
      intSet.a: 2
    }""")
    val failures = loadConfig[Set[Int]](conf, "pure.conf.intSet").left.value.toList
    failures should have size 1
    failures.head shouldBe a[CannotConvert]
  }

  "Converting from an empty string to a duration" should "complain about an empty string" in {
    val conf = ConfigFactory.parseMap(Map("i" -> "").asJava)
    val failures = loadConfig[ConfWithDuration](conf).left.value.toList
    failures should have size 1
    failures.head shouldBe a[EmptyStringFound]
  }

  "Converting from a list to Double" should "give a terrible error message, unfortunately" in {
    val conf = ConfigFactory.parseString("""{ "v": [1, 2, 3, 4] }""")
    val failures = loadConfig[ConfigWithDouble](conf).left.value.toList
    failures should have size 1
    failures.head shouldBe a[CannotConvert]
  }

  "Converting from a list to FiniteDuration" should "give an middling error message with poor context, unfortunately" in {
    val conf = ConfigFactory.parseString("""{ "timeout": [1, 2, 3, 4] }""")
    val failures = loadConfig[SparkNetwork](conf).left.value.toList
    failures should have size 1
    failures.head shouldBe a[CannotConvert]
  }

  "Converting an input of 'Inf'" should "produce an infinite Duration" in {
    val conf = ConfigFactory.parseString("""{ i: Inf }""")
    loadConfig[ConfWithDuration](conf).right.value.i.isFinite shouldBe false
  }
  it should "fail for a FiniteDuration" in {
    val conf = ConfigFactory.parseString("""{ timeout: Inf }""")
    val failures = loadConfig[SparkNetwork](conf).left.value.toList
    failures should have size 1
    failures.head shouldBe a[CannotConvert]
  }
}
