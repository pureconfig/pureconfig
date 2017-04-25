package pureconfig

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import org.scalacheck.{ Arbitrary, Gen }

class ConfigConvertSuite extends BaseSuite {
  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  val intConvert = ConfigConvert[Int]

  // generate configs that always read correctly as strings, but not always as integers
  val genConfig: Gen[ConfigValue] =
    Gen.frequency(80 -> Gen.chooseNum(Int.MinValue, Int.MaxValue), 20 -> Gen.alphaStr)
      .map(ConfigValueFactory.fromAnyRef)

  implicit val arbConfig = Arbitrary(genConfig)

  behavior of "ConfigConvert"

  it should "have a correct xmap method" in forAll { (f: Int => String, g: String => Int) =>
    forAll { str: String => intConvert.xmap(f, g).to(str) shouldEqual intConvert.to(g(str)) }
    forAll { conf: ConfigValue => intConvert.xmap(f, g).from(conf) shouldEqual intConvert.from(conf).right.map(f) }
  }
}
