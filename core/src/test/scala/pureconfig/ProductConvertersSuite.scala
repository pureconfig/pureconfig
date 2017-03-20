package pureconfig

import org.joda.time.format.ISODateTimeFormat
import org.scalacheck.Arbitrary
import org.scalacheck.Shapeless._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ EitherValues, FlatSpec, Matchers }
import pureconfig.ConfigConvert.{ catchReadError, fromStringConvert, fromStringReader }
import pureconfig.arbitrary._

import scala.collection.immutable.Seq
import scala.concurrent.duration.FiniteDuration

class ProductConvertersSuite extends FlatSpec with ConfigConvertChecks with Matchers with EitherValues with GeneratorDrivenPropertyChecks {

  behavior of "ConfigConvert"

  /* A configuration with only simple values and `Option` */
  case class FlatConfig(b: Boolean, d: Double, f: Float, i: Int, l: Long, s: String, o: Option[String])

  /* A configuration with a field of a type that is unknown to `ConfigConvert` */
  case class ConfigWithUnknownType(d: org.joda.time.DateTime)

  // a realistic example of configuration: common available Spark properties
  case class DriverConf(cores: Int, maxResultSize: String, memory: String)
  case class ExecutorConf(memory: String, extraJavaOptions: String)
  case class SparkAppConf(name: String)
  case class SparkLocalConf(dir: String)
  case class SparkNetwork(timeout: FiniteDuration)
  case class SparkConf(master: String, app: SparkAppConf, local: SparkLocalConf, driver: DriverConf, executor: ExecutorConf, extraListeners: Seq[String], network: SparkNetwork)
  case class SparkRootConf(spark: SparkConf)

  // arbitrary

  implicitly[Arbitrary[FlatConfig]]
  implicitly[Arbitrary[ConfigWithUnknownType]]

  // tests

  checkArbitrary[FlatConfig]

  implicit val dateConfigConvert = fromStringConvert[org.joda.time.DateTime](
    catchReadError(org.joda.time.DateTime.parse), ISODateTimeFormat.dateTime().print)
  checkArbitrary[ConfigWithUnknownType]

  it should s"be able to override all of the ConfigConvert instances used to parse ${classOf[FlatConfig]}" in forAll {
    (config: FlatConfig) =>
      implicit val readBoolean = fromStringReader[Boolean](catchReadError(_ => false))
      implicit val readDouble = fromStringReader[Double](catchReadError(_ => 1D))
      implicit val readFloat = fromStringReader[Float](catchReadError(_ => 2F))
      implicit val readInt = fromStringReader[Int](catchReadError(_ => 3))
      implicit val readLong = fromStringReader[Long](catchReadError(_ => 4L))
      implicit val readString = fromStringReader[String](catchReadError(_ => "foobar"))
      implicit val readOption = fromStringConvert[Option[String]](catchReadError(_ => None), _ => " ")
      val cc = ConfigConvert[FlatConfig]
      cc.from(cc.to(config)) shouldBe Right(FlatConfig(false, 1D, 2F, 3, 4L, "foobar", None))
  }

  //  case class ConfWithCamelCaseInner(thisIsAnInt: Int, thisIsAnotherInt: Int)
  //  case class ConfWithCamelCase(camelCaseInt: Int, camelCaseString: String, camelCaseConf: ConfWithCamelCaseInner)
  //
  //  // product hint, field mapping
  //  it should "read kebab case config keys to camel case fields by default" in {
  //    import pureconfig.syntax._
  //
  //    val conf = ConfigFactory.parseString("""{
  //      camel-case-int = 1
  //      camel-case-string = "bar"
  //      camel-case-conf {
  //        this-is-an-int = 3
  //        this-is-another-int = 10
  //      }
  //    }""")
  //
  //    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  //  }
  //
  //  // product hint, field mapping
  //  it should "allow customizing the field mapping through a product hint" in {
  //    val conf = ConfigFactory.parseString("""{
  //      A = 2
  //      B = "two"
  //    }""")
  //
  //    case class SampleConf(a: Int, b: String)
  //    loadConfig[SampleConf](conf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("a", None), KeyNotFound("b", None))
  //
  //    implicit val productHint = ProductHint[SampleConf](ConfigFieldMapping(_.toUpperCase))
  //
  //    loadConfig[SampleConf](conf) shouldBe Right(SampleConf(2, "two"))
  //  }
  //
  //  // product hint, field mapping
  //  it should "allow customizing the field mapping with different naming conventions" in {
  //    import pureconfig.syntax._
  //
  //    {
  //      implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  //
  //      val conf = ConfigFactory.parseString("""{
  //        camelCaseInt = 1
  //        camelCaseString = "bar"
  //        camelCaseConf {
  //          thisIsAnInt = 3
  //          thisIsAnotherInt = 10
  //        }
  //      }""")
  //
  //      conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  //    }
  //
  //    {
  //      implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, PascalCase))
  //
  //      val conf = ConfigFactory.parseString(
  //        """{
  //          CamelCaseInt = 1
  //          CamelCaseString = "bar"
  //          CamelCaseConf {
  //            ThisIsAnInt = 3
  //            ThisIsAnotherInt = 10
  //          }
  //        }""")
  //
  //      conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  //    }
  //  }
  //
  //  // product hint, field mapping
  //  it should "allow customizing the field mapping only for specific types" in {
  //    import pureconfig.syntax._
  //
  //    implicit val productHint = ProductHint[ConfWithCamelCase](ConfigFieldMapping(CamelCase, CamelCase))
  //
  //    val conf = ConfigFactory.parseString("""{
  //      camelCaseInt = 1
  //      camelCaseString = "bar"
  //      camelCaseConf {
  //        this-is-an-int = 3
  //        this-is-another-int = 10
  //      }
  //    }""")
  //
  //    conf.to[ConfWithCamelCase] shouldBe Right(ConfWithCamelCase(1, "bar", ConfWithCamelCaseInner(3, 10)))
  //  }
  //
  //  // product hint, unknown keys
  //  it should "disallow unknown keys if specified through a product hint" in {
  //    import pureconfig.syntax._
  //
  //    case class Conf1(a: Int)
  //    case class Conf2(a: Int)
  //
  //    implicit val productHint = ProductHint[Conf2](allowUnknownKeys = false)
  //
  //    val conf = ConfigFactory.parseString("""{
  //      conf {
  //        a = 1
  //        b = 2
  //      }
  //    }""")
  //
  //    conf.getConfig("conf").to[Conf1] shouldBe Right(Conf1(1))
  //    val failures = conf.getConfig("conf").to[Conf2].left.value.toList
  //    failures should have size 1
  //    failures.head shouldBe a[UnknownKey]
  //  }
  //
  //  val expectedValueForResolveFilesPriority2 = FlatConfig(
  //    false,
  //    0.001d,
  //    99.99f,
  //    42,
  //    1234567890123456L,
  //    "cheese",
  //    Some("thing"))
  //
  //  case class FooBar(foo: Foo, bar: Bar)
  //  case class ConfWithConfigObject(conf: ConfigObject)
  //  case class ConfWithConfigList(conf: ConfigList)
  //
  //  // allow missing key
  //  it should s"return a ${classOf[KeyNotFound]} when a key is not in the configuration" in {
  //    val emptyConf = ConfigFactory.empty()
  //    loadConfig[Foo](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("i", None))
  //    val conf = ConfigFactory.parseMap(Map("namespace.foo" -> 1).asJava)
  //    loadConfig[Foo](conf, "namespace").left.value.toList should contain theSameElementsAs Seq(KeyNotFound("namespace.i", None))
  //    loadConfig[ConfWithMapOfFoo](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("map", None))
  //    loadConfig[ConfWithListOfFoo](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("list", None))
  //    loadConfig[ConfWithConfigObject](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("conf", None))
  //    loadConfig[ConfWithConfigList](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("conf", None))
  //    loadConfig[ConfWithDuration](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("i", None))
  //    loadConfig[SparkNetwork](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("timeout", None))
  //
  //    case class InnerConf(v: Int)
  //    case class EnclosingConf(conf: InnerConf)
  //
  //    implicit val conv = new ConfigConvert[InnerConf] {
  //      def from(cv: ConfigValue) = Right(InnerConf(42))
  //      def to(conf: InnerConf) = ConfigFactory.parseString(s"{ v: ${conf.v} }").root()
  //    }
  //
  //    loadConfig[EnclosingConf](emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("conf", None))
  //  }
  //
  //  // allow missing key
  //  it should "allow custom ConfigConverts to handle missing keys" in {
  //    case class Conf(a: Int, b: Int)
  //    val conf = ConfigFactory.parseString("""{ a: 1 }""")
  //    loadConfig[Conf](conf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("b", None))
  //
  //    implicit val defaultInt = new ConfigConvert[Int] with AllowMissingKey {
  //      def from(v: ConfigValue) =
  //        if (v == null) Right(42) else {
  //          val s = v.render(ConfigRenderOptions.concise)
  //          catchReadError(_.toInt)(implicitly)(s)(None).left.map(ConfigReaderFailures.apply)
  //        }
  //      def to(v: Int) = ???
  //    }
  //    loadConfig[Conf](conf).right.value shouldBe Conf(1, 42)
  //  }
  //
  //  // wrong type
  //  it should s"return a ${classOf[WrongTypeForKey]} when a key has a wrong type" in {
  //    val conf = ConfigFactory.parseMap(Map("foo.i" -> 1, "bar.foo" -> "").asJava)
  //    val failures = loadConfig[FooBar](conf).left.value.toList
  //    failures should have size 1
  //    failures.head shouldBe a[WrongTypeForKey]
  //
  //    val conf1 = ConfigFactory.parseMap(Map("ns.foo.i" -> 1, "ns.bar.foo" -> "").asJava)
  //    val failures1 = loadConfig[FooBar](conf1, "ns").left.value.toList
  //    failures1 should have size 1
  //    failures1.head shouldBe a[WrongTypeForKey]
  //
  //    val conf2 = ConfigFactory.parseString("""{ map: [{ i: 1 }, { i: 2 }, { i: 3 }] }""")
  //    val failures2 = loadConfig[ConfWithMapOfFoo](conf2).left.value.toList
  //    failures2 should have size 1
  //    failures2.head shouldBe a[WrongTypeForKey]
  //
  //    val conf3 = ConfigFactory.parseString("""{ conf: [{ i: 1 }, { i: 2 }, { i: 3 }] }""")
  //    val failures3 = loadConfig[ConfWithConfigObject](conf3).left.value.toList
  //    failures3 should have size 1
  //    failures3.head shouldBe a[WrongTypeForKey]
  //
  //    val conf4 = ConfigFactory.parseString("""{ conf: { a: 1, b: 2 }}""")
  //    val failures4 = loadConfig[ConfWithConfigList](conf4).left.value.toList
  //    failures4 should have size 1
  //    failures4.head shouldBe a[WrongTypeForKey]
  //  }
  //
  //  // default arguments
  //  it should "consider default arguments by default" in {
  //    case class InnerConf(e: Int, g: Int)
  //    case class Conf(a: Int, b: String = "default", c: Int = 42, d: InnerConf = InnerConf(43, 44))
  //
  //    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava)
  //    loadConfig[Conf](conf1).right.value shouldBe Conf(2, "default", 42, InnerConf(43, 44))
  //
  //    val conf2 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50).asJava)
  //    loadConfig[Conf](conf2).right.value shouldBe Conf(2, "default", 50, InnerConf(43, 44))
  //
  //    val conf3 = ConfigFactory.parseMap(Map("c" -> 50).asJava)
  //    loadConfig[Conf](conf3).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("a", None))
  //
  //    val conf4 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5).asJava)
  //    loadConfig[Conf](conf4).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("d.g", None))
  //
  //    val conf5 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5, "d.g" -> 6).asJava)
  //    loadConfig[Conf](conf5).right.value shouldBe Conf(2, "default", 42, InnerConf(5, 6))
  //
  //    val conf6 = ConfigFactory.parseMap(Map("a" -> 2, "d" -> "notAnInnerConf").asJava)
  //    val failures = loadConfig[Conf](conf6).left.value.toList
  //    failures should have size 1
  //    failures.head shouldBe a[WrongTypeForKey]
  //  }
  //
  //  // product hint, default arguments
  //  it should "not use default arguments if specified through a product hint" in {
  //    case class InnerConf(e: Int, g: Int)
  //    case class Conf(a: Int, b: String = "default", c: Int = 42, d: InnerConf = InnerConf(43, 44))
  //
  //    implicit val productHint = ProductHint[Conf](useDefaultArgs = false)
  //
  //    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava)
  //    loadConfig[Conf](conf1).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("b", None), KeyNotFound("c", None), KeyNotFound("d", None))
  //  }

  //  it should s"be able to save ${classOf[Config2]} and load ${classOf[Config]} when namespace is set to config" in {
  //    withTempFile { configFile =>
  //      val expectedConfig = Config(new DateTime(1), List(1, 2, 3), Set(4, 5, 6), FlatConfig(false, 1d, 2f, 3, 4l, "5", Option("6")))
  //      val configToSave = Config2(expectedConfig)
  //      saveConfigAsPropertyFile(configToSave, configFile, overrideOutputPath = true)
  //      val config = loadConfig[Config](configFile, "config")
  //
  //      config should be(Right(expectedConfig))
  //    }
  //  }
  //
  //  // ** save and load, product
  //  it should s"be able to save and load ${classOf[SparkRootConf]}" in {
  //    withTempFile { configFile =>
  //
  //      val writer = new PrintWriter(Files.newOutputStream(configFile))
  //      writer.println("""spark.executor.extraJavaOptions=""""")
  //      writer.println("""spark.driver.maxResultSize="2g"""")
  //      writer.println("""spark.extraListeners=[]""")
  //      writer.println("""spark.app.name="myApp"""")
  //      writer.println("""spark.driver.memory="1g"""")
  //      writer.println("""spark.driver.cores="10"""")
  //      writer.println("""spark.master="local[*]"""")
  //      writer.println("""spark.executor.memory="2g"""")
  //      writer.println("""spark.local.dir="/tmp/"""")
  //      writer.println("""spark.network.timeout=45s""")
  //      // unused configuration
  //      writer.println("""akka.loggers = ["akka.event.Logging$DefaultLogger"]""")
  //      writer.close()
  //
  //      implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  //      val configOrError = loadConfig[SparkRootConf](configFile)
  //
  //      val config = configOrError match {
  //        case Left(f) => fail(f.toString)
  //        case Right(c) => c
  //      }
  //
  //      config.spark.executor.extraJavaOptions should be("")
  //      config.spark.driver.maxResultSize should be("2g")
  //      config.spark.extraListeners should be(Seq.empty[String])
  //      config.spark.app.name should be("myApp")
  //      config.spark.driver.memory should be("1g")
  //      config.spark.driver.cores should be(10)
  //      config.spark.master should be("local[*]")
  //      config.spark.executor.memory should be("2g")
  //      config.spark.local.dir should be("/tmp/")
  //      config.spark.network.timeout should be(FiniteDuration(45, TimeUnit.SECONDS))
  //    }
  //  }

  //  // traversable of complex types
  //
  //  case class Foo(i: Int)
  //  case class ConfWithListOfPair(list: List[(String, Int)])
  //
  //  case class ConfWithListOfFoo(list: List[Foo])
  //
  //  // ** save and load, default instance
  //  it should s"be able to load a list of Foo from a HOCON file" in {
  //    val conf = ConfigFactory.parseString("""{
  //      list = [{ i = 1 }, { i = 2 }, { i = 3 }]
  //    }""")
  //    val expected = ConfWithListOfFoo(List(Foo(1), Foo(2), Foo(3)))
  //    loadConfig[ConfWithListOfFoo](conf) shouldBe Right(expected)
  //  }
  //
  //  case class ConfWithStreamOfFoo(stream: Stream[Foo])
  //
  //  case class Bar(foo: Foo)
  //  case class ConfWithSetOfBar(set: Set[Bar])
  //  case class ConfWithQueueOfFoo(queue: Queue[Foo])
  //  case class ConfWithHashSetOfFoo(hashSet: HashSet[Foo])
  //  case class ConfWithListSetOfFoo(listSet: ListSet[Foo])
  //  case class ConfWithVectorOfFoo(vector: Vector[Foo])
  //  // map of complex types
  //
  //  case class ConfWithMapOfFoo(map: Map[String, Foo])
  //
  //  // ** save and load, default instance
  //  it should s"be able to save and load configurations containing map of Foo" in {
  //    saveAndLoadIsIdentity(ConfWithMapOfFoo(Map("a" -> Foo(1), "b" -> Foo(2))))
  //  }
  //
  //  case class ConfWithFoo(foo: Foo)
  //
  //  // ** save and load, override instance
  //  it should "be able to use a local ConfigConvert without getting an ImplicitResolutionFailure error" in {
  //    implicit val custom: ConfigConvert[Foo] = fromStringConvert(catchReadError(s => Foo(s.toInt)), _.i.toString)
  //    saveAndLoadIsIdentity(ConfWithFoo(Foo(100)))
  //  }
}
