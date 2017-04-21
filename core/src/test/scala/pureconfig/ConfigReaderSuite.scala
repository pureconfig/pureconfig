package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigValue, ConfigValueFactory }
import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.error.ConfigReaderFailures

class ConfigReaderSuite extends BaseSuite {
  val intReader = ConfigReader[Int]
  val strReader = ConfigReader[String]

  def intSummedReader(n: Int) = new ConfigReader[Int] {
    def from(config: ConfigValue) = intReader.from(config).right.map(_ + n)
  }

  // generate configs that always read correctly as strings, but not always as integers
  val genConfig: Gen[ConfigValue] =
    Gen.frequency(95 -> Gen.chooseNum(Int.MinValue, Int.MaxValue), 5 -> Gen.alphaStr)
      .map(ConfigValueFactory.fromAnyRef)

  implicit val arbConfig = Arbitrary(genConfig)

  behavior of "ConfigReader"

  it should "have a correct map method" in forAll { (conf: ConfigValue, f: Int => String) =>
    intReader.map(f).from(conf) === intReader.from(conf).right.map(f)
  }

  it should "have a correct flatMap method" in forAll { conf: ConfigValue =>
    val g = { n: Int => intSummedReader(n) }
    intReader.flatMap(g).from(conf) === intReader.from(conf).right.flatMap(g(_).from(conf))
  }

  it should "have a correct zip method" in forAll { conf: ConfigValue =>
    def zip[A, B](r1: ConfigReader[A], r2: ConfigReader[B]): Either[ConfigReaderFailures, (A, B)] = {
      (r1.from(conf), r2.from(conf)) match {
        case (Right(a), Right(b)) => Right((a, b))
        case (Left(fa), _) => Left(fa)
        case (_, Left(fb)) => Left(fb)
      }
    }

    intReader.zip(strReader).from(conf) === zip(intReader, strReader)
    strReader.zip(intReader).from(conf) === zip(strReader, intReader)
    intReader.zip(intReader).from(conf) === zip(intReader, intReader)
    strReader.zip(strReader).from(conf) === zip(strReader, strReader)
  }

  it should "have a correct orElse method" in forAll { conf: ConfigValue =>
    def orElse[AA, A <: AA, B <: AA](r1: ConfigReader[A], r2: ConfigReader[B]): Either[ConfigReaderFailures, AA] = {
      (r1.from(conf), r2.from(conf)) match {
        case (Right(a), _) => Right(a)
        case (Left(_), Right(b)) => Right(b)
        case (Left(fa), Left(fb)) => Left(fa ++ fb)
      }
    }

    // results are explicitly typed so that we also test the resulting type of `orElse`
    intReader.orElse(strReader).from(conf) === orElse[Any, Int, String](intReader, strReader)
    strReader.orElse(intReader).from(conf) === orElse[Any, String, Int](strReader, intReader)
    intReader.orElse(intReader).from(conf) === orElse[Int, Int, Int](intReader, intReader)
    strReader.orElse(strReader).from(conf) === orElse[String, String, String](strReader, strReader)
  }

  it should "have a correct contramapConfig method" in forAll { conf: ConfigValue =>
    val wrappedConf = ConfigFactory.parseString(s"{ value: ${conf.render} }").root()
    val unwrap = { cv: ConfigValue => cv.atKey("value").root() }

    intReader.contramapConfig(unwrap).from(wrappedConf) === intReader.from(conf)
  }
}
