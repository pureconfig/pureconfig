/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import java.io.PrintWriter
import java.net.URL
import java.nio.file.{ Path, Files }
import java.util.concurrent.TimeUnit

import com.typesafe.config._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import shapeless.{ Coproduct, :+:, CNil }

import scala.collection.JavaConverters._
import scala.collection.immutable._
import scala.util.Success
import scala.util.{ Failure, Try, Success }

import com.typesafe.config.ConfigFactory
import org.scalatest._
import pureconfig.ConfigConvert.{ fromString, stringConvert }

import scala.concurrent.duration.Duration

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

import PureconfSuite._

class PureconfSuite extends FlatSpec with Matchers with OptionValues with TryValues {

  // checks if saving and loading a configuration from file returns the configuration itself
  def saveAndLoadIsIdentity[C](config: C)(implicit configConvert: ConfigConvert[C]): Unit = {
    withTempFile { configFile =>
      saveConfigAsPropertyFile(config, configFile, overrideOutputPath = true)
      loadConfig[C](configFile) shouldEqual Success(config)
    }
  }

  // a simple "flat" configuration
  case class FlatConfig(b: Boolean, d: Double, f: Float, i: Int, l: Long, s: String, o: Option[String])

  "pureconfig" should s"be able to save and load ${classOf[FlatConfig]}" in {
    withTempFile { configFile =>
      val expectedConfig = FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6"))
      saveConfigAsPropertyFile(expectedConfig, configFile, overrideOutputPath = true)
      val config = loadConfig[FlatConfig](configFile)

      config should be(Success(expectedConfig))
    }
  }

  it should "be able to serialize a ConfigValue from a type with ConfigConvert using the toConfig method" in {
    import pureconfig.syntax._

    Map("a" -> 1, "b" -> 2).toConfig shouldBe ConfigFactory.parseString("""{ "a": 1, "b": 2 }""").root()
  }

  it should "be able to load a ConfigValue to a type with ConfigConvert using the to method" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ "a": [1, 2, 3, 4], "b": { "k1": "v1", "k2": "v2" } }""")
    conf.getList("a").to[List[Int]] shouldBe Success(List(1, 2, 3, 4))
    conf.getObject("b").to[Map[String, String]] shouldBe Success(Map("k1" -> "v1", "k2" -> "v2"))
  }

  it should "be able to load a Config to a type with ConfigConvert using the to method" in {
    import pureconfig.syntax._

    val conf = ConfigFactory.parseString("""{ "a": [1, 2, 3, 4], "b": { "k1": "v1", "k2": "v2" } }""")
    case class Conf(a: List[Int], b: Map[String, String])
    conf.to[Conf] shouldBe Success(Conf(List(1, 2, 3, 4), Map("k1" -> "v1", "k2" -> "v2")))
  }

  it should s"be able to override locally all of the ConfigConvert instances used to parse ${classOf[FlatConfig]}" in {
    implicit val readBoolean = fromString[Boolean](_ != "0")
    implicit val readDouble = fromString[Double](_.toDouble * -1)
    implicit val readFloat = fromString[Float](_.toFloat * -1)
    implicit val readInt = fromString[Int](_.toInt * -1)
    implicit val readLong = fromString[Long](_.toLong * -1)
    implicit val readString = fromString[String](_.toUpperCase)
    val config = loadConfig[FlatConfig](ConfigValueFactory.fromMap(Map(
      "b" -> 0,
      "d" -> 234.234,
      "f" -> 34.34,
      "i" -> 56,
      "l" -> -88,
      "s" -> "qwerTy").asJava).toConfig)

    config.success.value shouldBe FlatConfig(false, -234.234d, -34.34f, -56, 88L, "QWERTY", None)
  }

  // load HOCON-style lists
  case class ConfigWithHoconList(xs: List[Int])

  it should s"be able to load ${classOf[ConfigWithHoconList]}" in {
    withTempFile { configFile =>

      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.write("xs: [1, 2, 3]")
      writer.close()

      val config = loadConfig[ConfigWithHoconList](configFile)

      config should be(Success(ConfigWithHoconList(xs = List(1, 2, 3))))
    }
  }

  // a slightly more complex configuration
  implicit val dateConfigConvert = stringConvert[DateTime](
    str => ISODateTimeFormat.dateTime().parseDateTime(str),
    t => ISODateTimeFormat.dateTime().print(t))

  type ConfigCoproduct = Float :+: Boolean :+: CNil
  case class Config(d: DateTime, l: List[Int], s: Set[Int], subConfig: FlatConfig, coproduct: ConfigCoproduct)

  it should s"be able to save and load ${classOf[Config]}" in {
    withTempFile { configFile =>
      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")), Coproduct[ConfigCoproduct](false))
      saveConfigAsPropertyFile(expectedConfig, configFile, overrideOutputPath = true)
      val config = loadConfig[Config](configFile)

      config should be(Success(expectedConfig))
    }
  }

  // the same configuration but with custom namespace
  case class Config2(config: Config)

  it should s"be able to save ${classOf[Config2]} and load ${classOf[Config]} when namespace is set to config" in {
    withTempFile { configFile =>
      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")), Coproduct[ConfigCoproduct](false))
      val configToSave = Config2(expectedConfig)
      saveConfigAsPropertyFile(configToSave, configFile, overrideOutputPath = true)
      val config = loadConfig[Config](configFile, "config")

      config should be(Success(expectedConfig))
    }
  }

  // a realistic example of configuration: common available Spark properties
  case class DriverConf(cores: Int, maxResultSize: String, memory: String)
  case class ExecutorConf(memory: String, extraJavaOptions: String)
  case class SparkAppConf(name: String)
  case class SparkLocalConf(dir: String)
  case class SparkNetwork(timeout: Duration)
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
      writer.println("""spark.network.timeout="45s"""")
      // unused configuration
      writer.println("""akka.loggers = ["akka.event.Logging$DefaultLogger"]""")
      writer.close()

      val configOrError = loadConfig[SparkRootConf](configFile)

      val config = configOrError match {
        case Failure(f) => fail(f)
        case Success(c) => c
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
      config.spark.network.timeout should be(Duration(45, TimeUnit.SECONDS))
    }
  }

  case class MapConf(conf: Map[String, Int])

  it should s"be able to save and load Map like fields" in {
    withTempFile { configFile =>
      val expected = MapConf(Map("a" -> 1, "b" -> 2, "c" -> 3))
      saveConfigAsPropertyFile(expected, configFile, overrideOutputPath = true)
      val result = loadConfig[MapConf](configFile)

      result shouldEqual (Success(expected))
    }
  }

  it should s"not be able to load invalid Maps" in {
    // invalid value
    withTempFile { configFile =>
      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("conf.a=foo")
      writer.close()

      val result = loadConfig[MapConf](configFile)

      result.isFailure shouldEqual true
    }

    // invalid key because it contains the namespace separator
    withTempFile { configFile =>
      val writer = new PrintWriter(Files.newOutputStream(configFile))
      writer.println("conf.a.b=1")
      writer.close()

      val result = loadConfig[MapConf](configFile)

      result.isFailure shouldEqual true
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

      result shouldEqual Success(expected)
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

    result shouldEqual Success(expected)
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
    loadConfig[ConfWithListOfFoo](conf) shouldBe Success(expected)
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

  case class ConfWithStackOfFoo(stack: collection.mutable.Stack[Foo])

  it should s"be able to save and load configurations containing mutable.Stack" in {
    saveAndLoadIsIdentity(ConfWithStackOfFoo(collection.mutable.Stack(Foo(1))))
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
    implicit val custom: ConfigConvert[Foo] = stringConvert(s => Foo(s.toInt), _.i.toString)
    saveAndLoadIsIdentity(ConfWithFoo(Foo(100)))
  }

  case class ConfWithInt(i: Int)

  it should "be able to use a local ConfigConvert instead of the ones in ConfigConvert companion object" in {
    implicit val readInt = fromString[Int](_.toInt.abs)
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "-100").asJava).toConfig)(ConfigConvert[ConfWithInt]).success.value shouldBe ConfWithInt(100)
  }

  case class ConfWithDuration(i: Duration)

  it should "be able to supersede the default Duration ConfigConvert with a locally defined ConfigConvert from fromString" in {
    val expected = Duration(110, TimeUnit.DAYS)
    implicit val readDurationBadly = fromString[Duration](_ => expected)
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "23 s").asJava).toConfig)(ConfigConvert[ConfWithDuration]).success.value shouldBe ConfWithDuration(expected)
  }

  it should "be able to supersede the default Duration ConfigConvert with a locally defined ConfigConvert" in {
    val expected = Duration(220, TimeUnit.DAYS)
    implicit val readDurationBadly = new ConfigConvert[Duration] {
      override def from(config: ConfigValue): Try[Duration] = Success(expected)
      override def to(t: Duration): ConfigValue = throw new Exception("Not Implemented")
    }
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "42 h").asJava).toConfig)(ConfigConvert[ConfWithDuration]).success.value shouldBe ConfWithDuration(expected)
  }

  it should "custom ConfigConvert should not cause implicit resolution failure and should be used" in {
    implicit val custom: ConfigConvert[Foo] = new ConfigConvert[Foo] {
      def from(config: ConfigValue): Try[Foo] =
        Try(Foo(config.asInstanceOf[ConfigObject].get("i").render().toInt + 1))
      def to(foo: Foo): ConfigValue =
        ConfigValueFactory.fromMap(Map("i" -> foo.i).asJava)
    }
    loadConfig(ConfigFactory.parseString("foo.i = -100"))(ConfigConvert[ConfWithFoo]).success.value shouldBe ConfWithFoo(Foo(-99))
  }

  case class ConfWithURL(url: URL)

  it should "be able to read a config with a URL" in {
    val expected = "http://host/path?with=query&param"
    val config = loadConfig[ConfWithURL](ConfigValueFactory.fromMap(Map("url" -> expected).asJava).toConfig)
    config.toOption.value.url shouldBe new URL(expected)
  }

  it should "round trip a URL" in {
    saveAndLoadIsIdentity(ConfWithURL(new URL("https://you/spin?me&right=round")))
  }

  it should "allow a custom ConfigConvert[URL] to override our definition" in {
    val expected = "http://bad/horse/will?make=you&his=mare"
    implicit val readURLBadly = fromString[URL](_ => new URL(expected))
    val config = loadConfig[ConfWithURL](ConfigValueFactory.fromMap(Map("url" -> "https://ignored/url").asJava).toConfig)
    config.toOption.value.url shouldBe new URL(expected)
  }

  val expectedValueForResolveFilesPriority2 = FlatConfig(
    false,
    0.001d,
    99.99f,
    42,
    1234567890123456L,
    "cheese",
    Some("thing")
  )

  "loadConfigFromFiles" should "load a complete configuration from a single file" in {
    val files = fileList(
      "src/test/resources/conf/loadConfigFromFiles/priority2.conf"
    )
    loadConfigFromFiles[FlatConfig](files).success.get shouldBe expectedValueForResolveFilesPriority2
  }

  it should "fill in missing values from the lower priority files" in {
    val files = fileList(
      "src/test/resources/conf/loadConfigFromFiles/priority1.conf",
      "src/test/resources/conf/loadConfigFromFiles/priority2.conf"
    )
    val actual = loadConfigFromFiles[FlatConfig](files)
    actual.success.get shouldBe FlatConfig(
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
      "src/test/resources/conf/loadConfigFromFiles/priority1.conf"
    )
    val actual = loadConfigFromFiles[FlatConfig](files)
    actual.isFailure shouldBe true
  }

  it should "silently ignore files which can't be read" in {
    val files = fileList(
      "src/test/resources/conf/loadConfigFromFiles/this.is.not.a.conf",
      "src/test/resources/conf/loadConfigFromFiles/priority2.conf"
    )
    loadConfigFromFiles[FlatConfig](files).success.value shouldBe expectedValueForResolveFilesPriority2
  }

  it should "complain if the list of files is empty" in {
    val files = fileList()
    loadConfigFromFiles[FlatConfig](files).failure.exception.getMessage should include regex "config files.*must not be empty"
  }
}
