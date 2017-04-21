package pureconfig

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import org.scalacheck.{ Arbitrary, Gen }

class ConfigConvertSuite extends BaseSuite {
  val intConvert = ConfigConvert[Int]

  // generate configs that always read correctly as strings, but not always as integers
  val genConfig: Gen[ConfigValue] =
    Gen.frequency(95 -> Gen.chooseNum(Int.MinValue, Int.MaxValue), 5 -> Gen.alphaStr)
      .map(ConfigValueFactory.fromAnyRef)

  implicit val arbConfig = Arbitrary(genConfig)

  behavior of "ConfigConvert"

  it should "have a correct xmap method" in forAll { (f: Int => String, g: String => Int) =>
    forAll { str: String => intConvert.xmap(f, g).to(str) === intConvert.to(g(str)) }
    forAll { conf: ConfigValue => intConvert.xmap(f, g).from(conf) === intConvert.from(conf).right.map(f) }
  }
}
