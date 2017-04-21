package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigValue }

class ConfigWriterSuite extends BaseSuite {
  val intWriter = ConfigWriter[Int]

  behavior of "ConfigWriter"

  it should "have a correct contramap method" in forAll { (str: String, f: String => Int) =>
    intWriter.contramap(f).to(str) === intWriter.to(f(str))
  }

  it should "have a correct mapConfig method" in forAll { x: Int =>
    val wrap = { cv: ConfigValue => ConfigFactory.parseString(s"{ value: ${cv.render} }").root() }
    intWriter.mapConfig(wrap).to(x) === wrap(intWriter.to(x))
  }
}
