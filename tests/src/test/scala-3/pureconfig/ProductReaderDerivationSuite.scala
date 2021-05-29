package pureconfig
package generic

import scala.collection.JavaConverters.given
import scala.language.higherKinds

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import org.scalacheck.Arbitrary
import pureconfig._
import pureconfig.ConfigConvert.catchReadError
import pureconfig.error.{KeyNotFound, WrongType}
import pureconfig.generic.derivation.default.derived
import pureconfig.error.WrongSizeList

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
