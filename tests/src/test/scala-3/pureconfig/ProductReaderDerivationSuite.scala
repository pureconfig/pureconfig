package pureconfig

import scala.collection.JavaConverters.given

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import org.scalacheck.Arbitrary

import pureconfig.ConfigConvert.catchReadError
import pureconfig._
import pureconfig.error.{KeyNotFound, WrongSizeList, WrongType}

class ProductReaderDerivationSuite extends BaseSuite {

  behavior of "ConfigReader"

  it should s"be able to override all of the ConfigReader instances used to parse the product elements" in {
    case class FlatConfig(b: Boolean, d: Double, f: Float, i: Int, l: Long, s: String, o: Option[String])
        derives ConfigReader

    given ConfigReader[Boolean] = ConfigReader.fromString[Boolean](catchReadError(_ => false))
    given ConfigReader[Double] = ConfigReader.fromString[Double](catchReadError(_ => 1d))
    given ConfigReader[Float] = ConfigReader.fromString[Float](catchReadError(_ => 2f))
    given ConfigReader[Int] = ConfigReader.fromString[Int](catchReadError(_ => 3))
    given ConfigReader[Long] = ConfigReader.fromString[Long](catchReadError(_ => 4L))
    given ConfigReader[String] = ConfigReader.fromString[String](catchReadError(_ => "foobar"))
    given ConfigReader[Option[String]] = ConfigConvert.viaString[Option[String]](catchReadError(_ => None), _ => " ")

    val cc = ConfigReader[FlatConfig]
    val configValue =
      ConfigValueFactory.fromMap(
        Map(
          "b" -> true,
          "d" -> 2d,
          "f" -> 4f,
          "i" -> 6,
          "l" -> 8L,
          "s" -> "barfoo",
          "o" -> "foobar"
        ).asJava
      )
    cc.from(configValue) shouldBe Right(FlatConfig(false, 1d, 2f, 3, 4L, "foobar", None))
  }

  val emptyConf = ConfigFactory.empty().root()

  it should s"succeed with a correct config" in {
    case class Foo(i: Int, s: String, bs: List[Boolean]) derives ConfigReader
    val conf = ConfigFactory.parseString("""{ i: 1, s: "value", bs: [ true, false ] }""").root()
    ConfigReader[Foo].from(conf) shouldBe Right(Foo(1, "value", List(true, false)))
  }

  it should s"be able to read lists as tuples" in {
    case class Foo(values: (Boolean, Int)) derives ConfigReader
    val conf = ConfigFactory.parseString("""{ values: [ true, 5 ] }""").root()
    ConfigReader[Foo].from(conf) shouldBe Right(Foo(true -> 5))
  }

  it should s"return a ${classOf[WrongType]} if the types in the list do not match the tuple" in {
    case class Foo(values: (Boolean, Int)) derives ConfigReader
    val conf = ConfigFactory.parseString("""{ values: [ true, "value" ] }""").root()
    ConfigReader[Foo].from(conf) should failWithReason[WrongType]
  }

  it should s"return a ${classOf[WrongSizeList]} if the list is shorter than the tuple size" in {
    case class Foo(values: (Boolean, Int)) derives ConfigReader
    val conf = ConfigFactory.parseString("""{ values: [ true ] }""").root()
    ConfigReader[Foo].from(conf) should failWithReason[WrongSizeList]
  }

  it should s"return a ${classOf[WrongSizeList]} if the list is longer than the tuple size" in {
    case class Foo(values: (Boolean, Int)) derives ConfigReader
    val conf = ConfigFactory.parseString("""{ values: [ true, 5, "value" ] }""").root()
    ConfigReader[Foo].from(conf) should failWithReason[WrongSizeList]
  }

  it should s"return a ${classOf[KeyNotFound]} when a key is not in the configuration" in {
    case class Foo(i: Int) derives ConfigReader
    ConfigReader[Foo].from(emptyConf) should failWith(KeyNotFound("i"))
  }

  it should "use default arguments when a key is not in the configuration" in {
    case class Person(name: String, age: Int = -1) derives ConfigReader
    val conf = ConfigFactory.parseString("{ name: foo }").root()
    ConfigReader[Person].from(conf) shouldBe Right(Person("foo", -1))
  }

  it should "use default arguments for generic case classes when a key is not in the configuration" in {
    case class ConfB[T](i: Int = 1, s: String = "a", l: List[T] = Nil)
    given [T: ConfigReader]: ConfigReader[ConfB[T]] = ConfigReader.derived

    ConfigReader[ConfB[Boolean]].from(emptyConf).value shouldBe ConfB[Boolean]()
  }

  it should "consider default arguments by default" in {
    case class InnerConf(e: Int, g: Int)
    case class Conf(
        a: Int,
        b: String = "default",
        c: Int = 42,
        d: InnerConf = InnerConf(43, 44),
        e: Option[Int] = Some(45)
    ) derives ConfigReader

    val conf1 = ConfigFactory.parseMap(Map("a" -> 2).asJava).root()
    ConfigReader[Conf].from(conf1).value shouldBe Conf(2, "default", 42, InnerConf(43, 44), Some(45))

    val conf2 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50).asJava).root()
    ConfigReader[Conf].from(conf2).value shouldBe Conf(2, "default", 50, InnerConf(43, 44), Some(45))

    val conf3 = ConfigFactory.parseMap(Map("c" -> 50).asJava).root()
    ConfigReader[Conf].from(conf3) should failWith(KeyNotFound("a"))

    val conf4 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5).asJava).root()
    ConfigReader[Conf].from(conf4) should failWith(KeyNotFound("g"), "d.g")

    val conf5 = ConfigFactory.parseMap(Map("a" -> 2, "d.e" -> 5, "d.g" -> 6).asJava).root()
    ConfigReader[Conf].from(conf5).value shouldBe Conf(2, "default", 42, InnerConf(5, 6), Some(45))

    val conf6 = ConfigFactory.parseMap(Map("a" -> 2, "d" -> "notAnInnerConf").asJava).root()
    ConfigReader[Conf].from(conf6) should failWithReason[WrongType]

    val conf7 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50, "e" -> 1).asJava).root()
    ConfigReader[Conf].from(conf7).value shouldBe Conf(2, "default", 50, InnerConf(43, 44), Some(1))

    val conf8 = ConfigFactory.parseMap(Map("a" -> 2, "c" -> 50, "e" -> null).asJava).root()
    ConfigReader[Conf].from(conf8).value shouldBe Conf(2, "default", 50, InnerConf(43, 44), None)
  }

  it should "evaluate defaults lazily" in {
    def throwException: Nothing = throw new RuntimeException("Should not be evaluated")

    case class ConfA(foo: String = throwException) derives ConfigReader
    case class ConfB(bar: Option[ConfA]) derives ConfigReader
    case class ConfC(baz: ConfA = ConfA("a"), foo: String = throwException) derives ConfigReader

    ConfigSource.string("{ foo: bar }").load[ConfA] shouldBe Right(ConfA("bar"))
    ConfigSource.string("{ }").load[ConfB] shouldBe Right(ConfB(None))
    ConfigSource.string("{ baz: { foo: bar }, foo: bar }").load[ConfC] shouldBe Right(ConfC(ConfA("bar"), "bar"))
  }

  it should s"return a ${classOf[KeyNotFound]} when a custom convert is used and when a key is not in the configuration" in {
    case class InnerConf(v: Int)
    case class EnclosingConf(conf: InnerConf) derives ConfigReader

    given ConfigReader[InnerConf] with {
      def from(cv: ConfigCursor) = Right(InnerConf(42))
    }

    ConfigReader[EnclosingConf].from(emptyConf) should failWith(KeyNotFound("conf"))
  }

  it should "allow custom ConfigReaders to handle missing keys" in {
    case class Conf(a: Int, b: Int) derives ConfigReader
    val conf = ConfigFactory.parseString("""{ a: 1 }""").root()
    ConfigReader[Conf].from(conf) should failWith(KeyNotFound("b"))

    locally {
      given ConfigReader[Int] with ReadsMissingKeys with {
        def from(cur: ConfigCursor) =
          cur.asConfigValue.fold(
            _ => Right(42),
            v => {
              val s = v.render(ConfigRenderOptions.concise)
              cur.scopeFailure(catchReadError(_.toInt)(implicitly)(s))
            }
          )
      }
      given ConfigReader[Conf] = ConfigReader.derived[Conf]

      ConfigReader[Conf].from(conf).value shouldBe Conf(1, 42)
    }
  }

  it should s"return a ${classOf[WrongType]} when a key has a wrong type" in {
    case class Foo(i: Int)
    case class Bar(foo: Foo)
    case class FooBar(foo: Foo, bar: Bar) derives ConfigReader
    val conf = ConfigFactory.parseMap(Map("foo.i" -> 1, "bar.foo" -> "").asJava).root()
    ConfigReader[FooBar].from(conf) should failWithReason[WrongType]
  }

  it should s"work properly with recursively defined product types" in {
    case class RecType(ls: List[RecType]) derives ConfigReader
    val conf = ConfigFactory.parseString("ls = [{ ls = [] }, { ls = [{ ls = [] }] }]").root()
    ConfigReader[RecType].from(conf).value shouldBe RecType(List(RecType(Nil), RecType(List(RecType(Nil)))))
  }
}
