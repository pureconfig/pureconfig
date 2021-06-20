package pureconfig.module.magnolia

import scala.collection.JavaConverters._
import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import org.scalacheck.Arbitrary

import pureconfig.ConfigConvert.catchReadError
import pureconfig._
import pureconfig.error.{KeyNotFound, WrongType}
import pureconfig.module.magnolia.auto.reader._
import pureconfig.module.magnolia.auto.writer._

class ProductConvertersSuite extends BaseSuite {

  behavior of "ConfigConvert"

  /* A configuration with only simple values and `Option` */
  case class FlatConfig(b: Boolean, d: Double, f: Float, i: Int, l: Long, s: String, o: Option[String])

  /* A configuration with a field of a type that is unknown to `ConfigConvert` */
  class MyType(myField: String) {
    def getMyField: String = myField
    override def equals(obj: Any): Boolean =
      obj match {
        case mt: MyType => myField.equals(mt.getMyField)
        case _ => false
      }
  }
  case class ConfigWithUnknownType(d: MyType)

  case class RecType(ls: List[RecType])

  implicit val arbFlatConfig: Arbitrary[FlatConfig] = Arbitrary {
    Arbitrary.arbitrary[(Boolean, Double, Float, Int, Long, String, Option[String])].map((FlatConfig.apply _).tupled)
  }

  implicit val arbMyType: Arbitrary[MyType] = Arbitrary {
    Arbitrary.arbitrary[String].map(new MyType(_))
  }

  implicit val arbConfigWithUnknownType: Arbitrary[ConfigWithUnknownType] = Arbitrary {
    Arbitrary.arbitrary[MyType].map(ConfigWithUnknownType.apply)
  }

  // tests

  checkArbitrary[FlatConfig]

  implicit val myTypeConvert = ConfigConvert.viaString[MyType](catchReadError(new MyType(_)), _.getMyField)
  checkArbitrary[ConfigWithUnknownType]

  it should s"be able to override all of the ConfigConvert instances used to parse ${classOf[FlatConfig]}" in forAll {
    (config: FlatConfig) =>
      implicit val readBoolean = ConfigReader.fromString[Boolean](catchReadError(_ => false))
      implicit val readDouble = ConfigReader.fromString[Double](catchReadError(_ => 1d))
      implicit val readFloat = ConfigReader.fromString[Float](catchReadError(_ => 2f))
      implicit val readInt = ConfigReader.fromString[Int](catchReadError(_ => 3))
      implicit val readLong = ConfigReader.fromString[Long](catchReadError(_ => 4L))
      implicit val readString = ConfigReader.fromString[String](catchReadError(_ => "foobar"))
      implicit val readOption = ConfigConvert.viaString[Option[String]](catchReadError(_ => None), _ => " ")
      val cc = ConfigConvert[FlatConfig]
      cc.from(cc.to(config)) shouldBe Right(FlatConfig(false, 1d, 2f, 3, 4L, "foobar", None))
  }

  val emptyConf = ConfigFactory.empty().root()

  it should s"return a ${classOf[KeyNotFound]} when a key is not in the configuration" in {
    case class Foo(i: Int)
    ConfigConvert[Foo].from(emptyConf) should failWith(KeyNotFound("i"))
  }

  it should s"return a ${classOf[KeyNotFound]} when a custom convert is used and when a key is not in the configuration" in {
    case class InnerConf(v: Int)
    case class EnclosingConf(conf: InnerConf)

    implicit val conv = new ConfigConvert[InnerConf] {
      def from(cv: ConfigCursor) = Right(InnerConf(42))
      def to(conf: InnerConf) = ConfigFactory.parseString(s"{ v: ${conf.v} }").root()
    }

    ConfigConvert[EnclosingConf].from(emptyConf) should failWith(KeyNotFound("conf"))
  }

  it should "allow custom ConfigReaders to handle missing keys" in {
    case class Conf(a: Int, b: Int)
    val conf = ConfigFactory.parseString("""{ a: 1 }""").root()
    ConfigReader[Conf].from(conf) should failWith(KeyNotFound("b"))

    implicit val defaultInt = new ConfigReader[Int] with ReadsMissingKeys {
      def from(cur: ConfigCursor) =
        cur.asConfigValue.fold(
          _ => Right(42),
          v => {
            val s = v.render(ConfigRenderOptions.concise)
            cur.scopeFailure(catchReadError(_.toInt)(implicitly)(s))
          }
        )
    }
    ConfigReader[Conf].from(conf).value shouldBe Conf(1, 42)
  }

  it should "allow custom ConfigWriters to handle missing keys" in {
    case class Conf(a: Int, b: Int)
    ConfigWriter[Conf].to(Conf(0, 3)) shouldBe ConfigFactory.parseString("""{ a: 0, b: 3 }""").root()

    implicit val nonZeroInt = new ConfigWriter[Int] with WritesMissingKeys[Int] {
      def to(v: Int) = ConfigValueFactory.fromAnyRef(v)
      def toOpt(a: Int) = if (a == 0) None else Some(to(a))
    }
    ConfigWriter[Conf].to(Conf(0, 3)) shouldBe ConfigFactory.parseString("""{ b: 3 }""").root()
  }

  it should "not write empty option fields" in {
    case class Conf(a: Int, b: Option[Int])
    ConfigConvert[Conf].to(Conf(42, Some(1))) shouldBe ConfigFactory.parseString("""{ a: 42, b: 1 }""").root()
    ConfigConvert[Conf].to(Conf(42, None)) shouldBe ConfigFactory.parseString("""{ a: 42 }""").root()
  }

  it should s"return a ${classOf[WrongType]} when a key has a wrong type" in {
    case class Foo(i: Int)
    case class Bar(foo: Foo)
    case class FooBar(foo: Foo, bar: Bar)
    val conf = ConfigFactory.parseMap(Map("foo.i" -> 1, "bar.foo" -> "").asJava).root()
    ConfigConvert[FooBar].from(conf) should failWithReason[WrongType]
  }

  it should "consider default arguments by default" in {
    case class InnerConf(e: Int, g: Int)
    case class Conf(
        a: Int,
        b: String = "default",
        c: Int = 42,
        d: InnerConf = InnerConf(43, 44),
        e: Option[Int] = Some(45)
    )

    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava).root()
    ConfigConvert[Conf].from(conf1).value shouldBe Conf(2, "default", 42, InnerConf(43, 44), Some(45))

    val conf2 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50).asJava).root()
    ConfigConvert[Conf].from(conf2).value shouldBe Conf(2, "default", 50, InnerConf(43, 44), Some(45))

    val conf3 = ConfigFactory.parseMap(Map("c" -> 50).asJava).root()
    ConfigConvert[Conf].from(conf3) should failWith(KeyNotFound("a"))

    val conf4 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5).asJava).root()
    ConfigConvert[Conf].from(conf4) should failWith(KeyNotFound("g"), "d", emptyConfigOrigin)

    val conf5 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5, "d.g" -> 6).asJava).root()
    ConfigConvert[Conf].from(conf5).value shouldBe Conf(2, "default", 42, InnerConf(5, 6), Some(45))

    val conf6 = ConfigFactory.parseMap(Map("a" -> 2, "d" -> "notAnInnerConf").asJava).root()
    ConfigConvert[Conf].from(conf6) should failWithReason[WrongType]

    val conf7 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50, "e" -> 1).asJava).root()
    ConfigConvert[Conf].from(conf7).value shouldBe Conf(2, "default", 50, InnerConf(43, 44), Some(1))

    val conf8 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50, "e" -> null).asJava).root()
    ConfigConvert[Conf].from(conf8).value shouldBe Conf(2, "default", 50, InnerConf(43, 44), None)
  }

  it should s"work properly with recursively defined product types" in {
    val conf = ConfigFactory.parseString("ls = [{ ls = [] }, { ls = [{ ls = [] }] }]").root()
    ConfigConvert[RecType].from(conf).value shouldBe RecType(List(RecType(Nil), RecType(List(RecType(Nil)))))
  }
}
