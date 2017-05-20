package pureconfig.module.cats

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import org.scalacheck.{ Arbitrary, Cogen, Gen }
import org.scalacheck.Arbitrary.{ arbitrary => arb }
import pureconfig.{ ConfigConvert, ConfigReader, ConfigWriter }
import scala.collection.JavaConverters._

import pureconfig.error.ConfigReaderFailures

package object arbitrary {

  private[this] val MaxConfigDepth = 3
  private[this] val MaxCollSize = 3

  private[this] def genAnyForConfig(maxDepth: Int): Gen[Any] = {
    val genScalar: Gen[Any] = Gen.oneOf(
      Gen.oneOf(true, false),
      Gen.choose(Double.MinValue, Double.MaxValue),
      Gen.alphaStr)

    def genList(maxDepth: Int): Gen[List[Any]] = Gen.choose(0, MaxCollSize).flatMap { sz =>
      Gen.listOfN(sz, Gen.lzy(genAnyForConfig(maxDepth - 1)))
    }

    def genMap(maxDepth: Int): Gen[Map[String, Any]] = Gen.choose(0, MaxCollSize).flatMap { sz =>
      Gen.mapOfN(sz, for { k <- Gen.alphaStr; v <- Gen.lzy(genAnyForConfig(maxDepth - 1)) } yield (k, v))
    }

    if (maxDepth == 0) genScalar
    else Gen.frequency(
      3 -> genScalar,
      1 -> genList(maxDepth).map(_.asJava),
      1 -> genMap(maxDepth).map(_.asJava))
  }

  implicit val arbConfigValue: Arbitrary[ConfigValue] = Arbitrary {
    genAnyForConfig(MaxConfigDepth).map(ConfigValueFactory.fromAnyRef)
  }

  implicit val cogenConfigValue: Cogen[ConfigValue] =
    Cogen[String].contramap[ConfigValue](_.render)

  implicit val arbConfigReaderFailures: Arbitrary[ConfigReaderFailures] = Arbitrary {
    Gen.const(ConfigReaderFailures(EmptyTraversableFound("List", None, None)))
  }

  implicit val cogenConfigReaderFailures: Cogen[ConfigReaderFailures] =
    Cogen[String].contramap[ConfigReaderFailures](_.toString)

  implicit def arbConfigReader[A: Arbitrary]: Arbitrary[ConfigReader[A]] = Arbitrary {
    arb[ConfigValue => Either[ConfigReaderFailures, A]].map(ConfigReader.fromFunction)
  }

  implicit def arbConfigWriter[A: Cogen]: Arbitrary[ConfigWriter[A]] = Arbitrary {
    arb[A => ConfigValue].map(ConfigWriter.fromFunction)
  }

  implicit def arbConfigConvert[A: Arbitrary: Cogen]: Arbitrary[ConfigConvert[A]] = Arbitrary {
    for { reader <- arb[ConfigReader[A]]; writer <- arb[ConfigWriter[A]] }
      yield ConfigConvert.fromReaderAndWriter(reader, writer)
  }
}
