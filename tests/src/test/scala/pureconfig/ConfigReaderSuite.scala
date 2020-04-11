package pureconfig

import com.typesafe.config.{ ConfigFactory, ConfigObject, ConfigOriginFactory, ConfigParseOptions, ConfigValue, ConfigValueFactory }
import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.error._

class ConfigReaderSuite extends BaseSuite {
  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  val intReader = ConfigReader[Int]
  val strReader = ConfigReader[String]

  def intSummedReader(n: Int) = new ConfigReader[Int] {
    def from(cur: ConfigCursor) = intReader.from(cur).right.map(_ + n)
  }

  // generate configs that always read correctly as strings, but not always as integers
  val genConfig: Gen[ConfigValue] =
    Gen.frequency(80 -> Gen.chooseNum(Int.MinValue, Int.MaxValue), 20 -> Gen.alphaStr)
      .map(ConfigValueFactory.fromAnyRef)

  val genFailureReason: Gen[FailureReason] =
    Gen.const(UnknownKey(""))

  implicit val arbConfig = Arbitrary(genConfig)
  implicit val arbFailureReason = Arbitrary(genFailureReason)

  behavior of "ConfigReader"

  it should "have a correct map method" in forAll { (conf: ConfigValue, f: Int => String) =>
    intReader.map(f).from(conf) shouldEqual intReader.from(conf).right.map(f)
  }

  it should "have a map method that wraps exceptions in a ConfigReaderFailure" in {
    val throwable = new Exception("Exception message.")
    val cr = ConfigReader[Int].map({ _ => throw throwable })
    cr.from(ConfigValueFactory.fromAnyRef(1)) should failWith(ExceptionThrown(throwable))
  }

  it should "have a correct emap method" in forAll { (conf: ConfigValue, f: Int => Either[FailureReason, String]) =>
    def getReason[A](failures: ConfigReaderFailures): FailureReason = failures match {
      case ConfigReaderFailures(ConvertFailure(reason, _, _), Nil) => reason
      case _ => throw new Exception(s"Unexpected value: $failures")
    }
    intReader.emap(f).from(conf).left.map(getReason) shouldEqual
      intReader.from(conf).left.map(getReason).right.flatMap(f)
  }

  it should "have a correct flatMap method" in forAll { conf: ConfigValue =>
    val g: Int => ConfigReader[Int] = intSummedReader
    intReader.flatMap(g).from(conf) shouldEqual intReader.from(conf).right.flatMap(g(_).from(conf))
  }

  it should "have a correct zip method" in forAll { conf: ConfigValue =>
    def zip[A, B](r1: ConfigReader[A], r2: ConfigReader[B]): ConfigReader.Result[(A, B)] = {
      (r1.from(conf), r2.from(conf)) match {
        case (Right(a), Right(b)) => Right((a, b))
        case (Left(fa), Right(_)) => Left(fa)
        case (Right(_), Left(fb)) => Left(fb)
        case (Left(fa), Left(fb)) => Left(fa ++ fb)
      }
    }

    intReader.zip(strReader).from(conf) shouldEqual zip(intReader, strReader)
    strReader.zip(intReader).from(conf) shouldEqual zip(strReader, intReader)
    intReader.zip(intReader).from(conf) shouldEqual zip(intReader, intReader)
    strReader.zip(strReader).from(conf) shouldEqual zip(strReader, strReader)
  }

  it should "have a correct orElse method" in forAll { conf: ConfigValue =>
    def orElse[AA, A <: AA, B <: AA](r1: ConfigReader[A], r2: ConfigReader[B]): ConfigReader.Result[AA] = {
      (r1.from(conf), r2.from(conf)) match {
        case (Right(a), _) => Right(a)
        case (Left(_), Right(b)) => Right(b)
        case (Left(fa), Left(fb)) => Left(fa ++ fb)
      }
    }

    // results are explicitly typed so that we also test the resulting type of `orElse`
    intReader.orElse(strReader).from(conf) shouldEqual orElse[Any, Int, String](intReader, strReader)
    strReader.orElse(intReader).from(conf) shouldEqual orElse[Any, String, Int](strReader, intReader)
    intReader.orElse(intReader).from(conf) shouldEqual orElse[Int, Int, Int](intReader, intReader)
    strReader.orElse(strReader).from(conf) shouldEqual orElse[String, String, String](strReader, strReader)
  }

  it should "have a correct contramapConfig method" in forAll { conf: ConfigValue =>
    val wrappedConf = conf.atKey("value").root()
    val unwrap = { cv: ConfigValue => cv.asInstanceOf[ConfigObject].get("value") }

    intReader.contramapConfig(unwrap).from(wrappedConf) shouldEqual intReader.from(conf)
  }
}
