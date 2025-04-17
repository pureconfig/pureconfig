package pureconfig.module.cats

import scala.jdk.CollectionConverters._

import com.typesafe.config.{Config, ConfigValue, ConfigValueFactory}
import org.scalacheck.Arbitrary.{arbitrary => arb}
import org.scalacheck.{Arbitrary, Cogen, Gen}

import pureconfig._
import pureconfig.error.{ConfigReaderFailures, ConvertFailure}

package object arbitrary {

  private[this] val MaxConfigDepth = 3
  private[this] val MaxCollSize = 3

  private[this] def genAnyMapForConfig(maxDepth: Int): Gen[Map[String, Any]] = {
    Gen.choose(0, MaxCollSize).flatMap { sz =>
      Gen.mapOfN(sz, for { k <- Gen.alphaStr; v <- Gen.lzy(genAnyForConfig(maxDepth - 1)) } yield (k, v))
    }
  }

  private[this] def genAnyForConfig(maxDepth: Int): Gen[Any] = {
    val genScalar: Gen[Any] = Gen.oneOf(
      Gen.oneOf(true, false),
      Gen.choose(Int.MinValue, Int.MaxValue),
      Gen.choose(Double.MinValue, Double.MaxValue),
      Gen.alphaStr
    )

    def genList(maxDepth: Int): Gen[List[Any]] =
      Gen.choose(0, MaxCollSize).flatMap { sz =>
        Gen.listOfN(sz, Gen.lzy(genAnyForConfig(maxDepth - 1)))
      }

    if (maxDepth == 0) genScalar
    else
      Gen.frequency(
        3 -> genScalar,
        1 -> genList(maxDepth).map(_.asJava),
        1 -> genAnyMapForConfig(maxDepth).map(_.asJava)
      )
  }

  implicit val arbConfigValue: Arbitrary[ConfigValue] = Arbitrary {
    genAnyForConfig(MaxConfigDepth).map(ConfigValueFactory.fromAnyRef)
  }

  implicit val arbConfig: Arbitrary[Config] = Arbitrary {
    genAnyMapForConfig(MaxConfigDepth).map(_.asJava).map(ConfigValueFactory.fromMap(_).toConfig)
  }

  implicit val cogenConfigValue: Cogen[ConfigValue] =
    Cogen[String].contramap[ConfigValue](_.render)

  implicit val arbConfigReaderFailures: Arbitrary[ConfigReaderFailures] = Arbitrary {
    Gen.const(ConfigReaderFailures(ConvertFailure(EmptyTraversableFound("List"), None, "")))
  }

  implicit val cogenConfigReaderFailures: Cogen[ConfigReaderFailures] =
    Cogen[String].contramap[ConfigReaderFailures](_.toString)

  implicit val arbConfigObjectSource: Arbitrary[ConfigObjectSource] = Arbitrary {
    Gen.either(arb[ConfigReaderFailures], arb[Config]).map(ConfigObjectSource(_))
  }

  implicit def arbConfigReader[A: Arbitrary]: Arbitrary[ConfigReader[A]] =
    Arbitrary {
      arb[ConfigValue => ConfigReader.Result[A]].map(ConfigReader.fromFunction)
    }

  implicit def arbConfigWriter[A: Cogen]: Arbitrary[ConfigWriter[A]] =
    Arbitrary {
      arb[A => ConfigValue].map(ConfigWriter.fromFunction)
    }

  implicit def arbConfigConvert[A: Arbitrary: Cogen]: Arbitrary[ConfigConvert[A]] =
    Arbitrary {
      for { reader <- arb[ConfigReader[A]]; writer <- arb[ConfigWriter[A]] } yield ConfigConvert.fromReaderAndWriter(
        reader,
        writer
      )
    }
}
