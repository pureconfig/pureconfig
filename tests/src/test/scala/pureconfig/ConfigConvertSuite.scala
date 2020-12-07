package pureconfig

import com.typesafe.config.{ConfigValue, ConfigValueFactory, ConfigValueType}
import org.scalacheck.{Arbitrary, Gen}
import pureconfig.ConfigConvertSuite._
import pureconfig.error.{CannotConvert, ExceptionThrown, WrongType}

class ConfigConvertSuite extends BaseSuite {
  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  val intConvert = ConfigConvert[Int]

  // generate configs that always read correctly as strings, but not always as integers
  val genConfig: Gen[ConfigValue] =
    Gen
      .frequency(80 -> Gen.chooseNum(Int.MinValue, Int.MaxValue), 20 -> Gen.alphaStr)
      .map(ConfigValueFactory.fromAnyRef)

  implicit val arbConfig = Arbitrary(genConfig)

  behavior of "ConfigConvert"

  it should "have a correct xmap method" in forAll { (f: Int => String, g: String => Int) =>
    forAll { str: String => intConvert.xmap(f, g).to(str) shouldEqual intConvert.to(g(str)) }
    forAll { conf: ConfigValue => intConvert.xmap(f, g).from(conf) shouldEqual intConvert.from(conf).right.map(f) }
  }

  it should "have a xmap method that wraps exceptions in a ConfigReaderFailure" in {
    val throwable = new Exception("Exception message.")
    val cc = ConfigConvert[Int].xmap[String]({ _ => throw throwable }, { _: String => 42 })
    cc.from(ConfigValueFactory.fromAnyRef(1)) should failWith(ExceptionThrown(throwable))
    cc.from(ConfigValueFactory.fromAnyRef("test")) should failWith(
      WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER))
    )
  }

  it should "have a xemap method that allows specifying custom failure messages" in {
    ConfigConvert[EvenInt].from(ConfigValueFactory.fromAnyRef(1)) should
      failWith(CannotConvert("1", "EvenInt", EvenInt.err(1)))
  }

  it should "correctly read using xemap" in {
    ConfigConvert[EvenInt].from(ConfigValueFactory.fromAnyRef(2)) shouldEqual Right(EvenInt(2))
  }
}

object ConfigConvertSuite {
  case class EvenInt(i: Int) extends AnyVal
  object EvenInt {
    def err(i: Int): String = s"Cannot construct an EvenInt from $i because it's not even"

    def safely(i: Int): Either[String, EvenInt] =
      if (i % 2 == 0) Right(EvenInt(i)) else Left(err(i))

    implicit val configConvert: ConfigConvert[EvenInt] =
      ConfigConvert[Int].xemap(i => safely(i).left.map(s => CannotConvert(i.toString, "EvenInt", s)), _.i)
  }
}
