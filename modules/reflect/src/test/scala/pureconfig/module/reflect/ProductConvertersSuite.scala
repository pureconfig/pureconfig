package pureconfig.module.reflect

import scala.collection.JavaConverters._
import com.typesafe.config.{ ConfigFactory, ConfigRenderOptions }
import pureconfig.{ BaseSuite, ConfigConvert, ConfigCursor, ConfigReader, ConfigWriter, ReadsMissingKeys }
import org.scalacheck.ScalacheckShapeless._
import pureconfig.ConfigConvert.catchReadError
import pureconfig.error.{ KeyNotFound, WrongType }

//noinspection TypeAnnotation
class ProductConvertersSuite
  extends BaseSuite {
  behavior of "ConfigConvert"

  /* A configuration with only simple values and `Option` */
  case class FlatConfig(
      b: Boolean,
      d: Double,
      f: Float,
      i: Int,
      l: Long,
      s: String,
      o: Option[String])

  // tests
  {
    //Make the implicit scope local
    implicit val flatConfigReader = ReflectConfigReaders.configReader7(FlatConfig)
    implicit val flatConfigWriter = ReflectConfigWriters.configWriter7((FlatConfig.unapply _).andThen(_.get))
    checkArbitrary[FlatConfig]
  }

  it should s"be able to override all of the ConfigConvert instances used to parse ${classOf[FlatConfig]}" in forAll {
    (config: FlatConfig) =>
      implicit val readBoolean = ConfigReader.fromString[Boolean](catchReadError(_ => false))
      implicit val readDouble = ConfigReader.fromString[Double](catchReadError(_ => 1D))
      implicit val readFloat = ConfigReader.fromString[Float](catchReadError(_ => 2F))
      implicit val readInt = ConfigReader.fromString[Int](catchReadError(_ => 3))
      implicit val readLong = ConfigReader.fromString[Long](catchReadError(_ => 4L))
      implicit val readString = ConfigReader.fromString[String](catchReadError(_ => "foobar"))
      implicit val readOption = ConfigConvert.viaString[Option[String]](catchReadError(_ => None), _ => " ")

      implicit val flatConfigReader = ReflectConfigReaders.configReader7(FlatConfig)
      implicit val flatConfigWriter = ReflectConfigWriters.configWriter7((FlatConfig.unapply _).andThen(_.get))
      val cc = ConfigConvert[FlatConfig]
      cc.from(cc.to(config)) shouldBe Right(FlatConfig(false, 1D, 2F, 3, 4L, "foobar", None))
  }

  val emptyConf = ConfigFactory.empty().root()

  it should s"return a ${classOf[KeyNotFound]} when a key is not in the configuration" in {
    case class Foo(i: Int)
    implicit val reader: ConfigReader[Foo] = ReflectConfigReaders.configReader1(Foo)
    implicit val writer: ConfigWriter[Foo] = ReflectConfigWriters.configWriter1((Foo.unapply _).andThen(_.get))
    ConfigConvert[Foo].from(emptyConf) should failWith(KeyNotFound("i"))
  }

  it should s"return a ${classOf[KeyNotFound]} when a custom convert is used and when a key is not in the configuration" in {
    case class InnerConf(v: Int)
    case class EnclosingConf(conf: InnerConf)

    implicit val conv = new ConfigConvert[InnerConf] {
      def from(cv: ConfigCursor) = Right(InnerConf(42))
      def to(conf: InnerConf) = ConfigFactory.parseString(s"{ v: ${conf.v} }").root()
    }

    implicit val reader = ReflectConfigReaders.configReader1(EnclosingConf)
    implicit val writer = ReflectConfigWriters.configWriter1((EnclosingConf.unapply _).andThen(_.get))

    ConfigConvert[EnclosingConf].from(emptyConf) should failWith(KeyNotFound("conf"))
  }

  it should "allow custom ConfigReaders to handle missing keys" in {
    case class Conf(a: Int, b: Int)
    val conf = ConfigFactory.parseString("""{ a: 1 }""").root()

    {
      implicit val reader = ReflectConfigReaders.configReader2(Conf)
      ConfigReader[Conf].from(conf) should failWith(KeyNotFound("b"))
    }

    implicit val defaultInt = new ConfigReader[Int] with ReadsMissingKeys {
      def from(cur: ConfigCursor) =
        if (cur.isUndefined) Right(42) else {
          val s = cur.value.render(ConfigRenderOptions.concise)
          cur.scopeFailure(catchReadError(_.toInt)(implicitly)(s))
        }
    }

    {
      implicit val reader = ReflectConfigReaders.configReader2(Conf)
      ConfigReader[Conf].from(conf).right.value shouldBe Conf(1, 42)
    }
  }

  it should "not write empty option fields" in {
    case class Conf(a: Int, b: Option[Int])

    implicit val reader = ReflectConfigReaders.configReader2(Conf)
    implicit val writer = ReflectConfigWriters.configWriter2((Conf.unapply _).andThen(_.get))

    ConfigConvert[Conf].to(Conf(42, Some(1))) shouldBe ConfigFactory.parseString("""{ a: 42, b: 1 }""").root()
    ConfigConvert[Conf].to(Conf(42, None)) shouldBe ConfigFactory.parseString("""{ a: 42 }""").root()
  }

  it should s"return a ${classOf[WrongType]} when a key has a wrong type" in {
    case class Foo(i: Int)
    case class Bar(foo: Foo)
    case class FooBar(foo: Foo, bar: Bar)

    implicit val readerFoo = ReflectConfigReaders.configReader1(Foo)
    implicit val readerBar = ReflectConfigReaders.configReader1(Bar)
    implicit val readerFooBar = ReflectConfigReaders.configReader2(FooBar)

    val conf = ConfigFactory.parseMap(Map("foo.i" -> 1, "bar.foo" -> "").asJava).root()
    ConfigReader[FooBar].from(conf) should failWithType[WrongType]
  }

}
