package pureconfig

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

import com.typesafe.config.{ ConfigFactory, ConfigRenderOptions, ConfigValue }
import org.joda.time.format.ISODateTimeFormat
import org.scalacheck.Shapeless._
import pureconfig.ConfigConvert.catchReadError
import pureconfig.arbitrary._
import pureconfig.error.{ ConfigReaderFailures, KeyNotFound, WrongType }

class ProductConvertersSuite extends BaseSuite {

  behavior of "ConfigConvert"

  /* A configuration with only simple values and `Option` */
  case class FlatConfig(b: Boolean, d: Double, f: Float, i: Int, l: Long, s: String, o: Option[String])

  /* A configuration with a field of a type that is unknown to `ConfigConvert` */
  case class ConfigWithUnknownType(d: org.joda.time.DateTime)

  // tests

  checkArbitrary[FlatConfig]

  implicit val dateConfigConvert = ConfigConvert.viaString[org.joda.time.DateTime](
    catchReadError(org.joda.time.DateTime.parse), ISODateTimeFormat.dateTime().print)
  checkArbitrary[ConfigWithUnknownType]

  it should s"be able to override all of the ConfigConvert instances used to parse ${classOf[FlatConfig]}" in forAll {
    (config: FlatConfig) =>
      implicit val readBoolean = ConfigReader.fromString[Boolean](catchReadError(_ => false))
      implicit val readDouble = ConfigReader.fromString[Double](catchReadError(_ => 1D))
      implicit val readFloat = ConfigReader.fromString[Float](catchReadError(_ => 2F))
      implicit val readInt = ConfigReader.fromString[Int](catchReadError(_ => 3))
      implicit val readLong = ConfigReader.fromString[Long](catchReadError(_ => 4L))
      implicit val readString = ConfigReader.fromString[String](catchReadError(_ => "foobar"))
      implicit val readOption = ConfigConvert.viaString[Option[String]](catchReadError(_ => None), _ => " ")
      val cc = ConfigConvert[FlatConfig]
      cc.from(cc.to(config)) shouldBe Right(FlatConfig(false, 1D, 2F, 3, 4L, "foobar", None))
  }

  val emptyConf = ConfigFactory.empty().root()

  it should s"return a ${classOf[KeyNotFound]} when a key is not in the configuration" in {
    case class Foo(i: Int)
    ConfigConvert[Foo].from(emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("i", None))
  }

  it should s"return a ${classOf[KeyNotFound]} when a custom convert is used and when a key is not in the configuration" in {
    case class InnerConf(v: Int)
    case class EnclosingConf(conf: InnerConf)

    implicit val conv = new ConfigConvert[InnerConf] {
      def from(cv: ConfigValue) = Right(InnerConf(42))
      def to(conf: InnerConf) = ConfigFactory.parseString(s"{ v: ${conf.v} }").root()
    }

    ConfigConvert[EnclosingConf].from(emptyConf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("conf", None))
  }

  it should "allow custom ConfigConverts to handle missing keys" in {
    case class Conf(a: Int, b: Int)
    val conf = ConfigFactory.parseString("""{ a: 1 }""").root()
    ConfigConvert[Conf].from(conf).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("b", None))

    implicit val defaultInt = new ConfigConvert[Int] with AllowMissingKey {
      def from(v: ConfigValue) =
        if (v == null) Right(42) else {
          val s = v.render(ConfigRenderOptions.concise)
          catchReadError(_.toInt)(implicitly)(s)(None).left.map(ConfigReaderFailures.apply)
        }
      def to(v: Int) = ???
    }
    ConfigConvert[Conf].from(conf).right.value shouldBe Conf(1, 42)
  }

  it should s"return a ${classOf[WrongType]} when a key has a wrong type" in {
    case class Foo(i: Int)
    case class Bar(foo: Foo)
    case class FooBar(foo: Foo, bar: Bar)
    val conf = ConfigFactory.parseMap(Map("foo.i" -> 1, "bar.foo" -> "").asJava).root()
    val failures = ConfigConvert[FooBar].from(conf).left.value.toList
    failures should have size 1
    failures.head shouldBe a[WrongType]
  }

  it should "consider default arguments by default" in {
    case class InnerConf(e: Int, g: Int)
    case class Conf(a: Int, b: String = "default", c: Int = 42, d: InnerConf = InnerConf(43, 44))

    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava).root()
    ConfigConvert[Conf].from(conf1).right.value shouldBe Conf(2, "default", 42, InnerConf(43, 44))

    val conf2 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50).asJava).root()
    ConfigConvert[Conf].from(conf2).right.value shouldBe Conf(2, "default", 50, InnerConf(43, 44))

    val conf3 = ConfigFactory.parseMap(Map("c" -> 50).asJava).root()
    ConfigConvert[Conf].from(conf3).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("a", None))

    val conf4 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5).asJava).root()
    ConfigConvert[Conf].from(conf4).left.value.toList should contain theSameElementsAs Seq(KeyNotFound("d.g", None))

    val conf5 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5, "d.g" -> 6).asJava).root()
    ConfigConvert[Conf].from(conf5).right.value shouldBe Conf(2, "default", 42, InnerConf(5, 6))

    val conf6 = ConfigFactory.parseMap(Map("a" -> 2, "d" -> "notAnInnerConf").asJava).root()
    val failures = ConfigConvert[Conf].from(conf6).left.value.toList
    failures should have size 1
    failures.head shouldBe a[WrongType]
  }
}
