package pureconfig

import com.typesafe.config.{ConfigFactory, ConfigValue}

class ConfigWriterSuite extends BaseSuite {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

  val intWriter = ConfigWriter[Int]

  behavior of "ConfigWriter"

  it should "have a correct contramap method" in forAll { (str: String, f: String => Int) =>
    intWriter.contramap(f).to(str) shouldEqual intWriter.to(f(str))
  }

  it should "have a correct mapConfig method" in forAll { (x: Int) =>
    val wrap = { (cv: ConfigValue) => ConfigFactory.parseString(s"{ value: ${cv.render} }").root() }
    intWriter.mapConfig(wrap).to(x) shouldEqual wrap(intWriter.to(x))
  }
}
