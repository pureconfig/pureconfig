package pureconfig.module.scalaz

import com.typesafe.config.{ConfigValue, ConfigValueFactory}
import org.scalacheck.{Arbitrary, Cogen, Gen}
import org.scalacheck.Arbitrary.{arbitrary => arb}
import pureconfig.{ConfigConvert, ConfigReader, ConfigWriter, Derivation}
import pureconfig.error._

import scala.collection.JavaConverters._
import scalaz.scalacheck.ScalaCheckBinding.GenMonad
import scalaz.syntax.applicative._

package object arbitrary {

  private[this] val MaxConfigDepth = 3
  private[this] val MaxCollectionLength = 3

  private[this] def genAny(depth: Int): Gen[Any] = {
    val genScalar: Gen[Any] =
      Gen.oneOf(Gen.oneOf(true, false), Gen.choose(Double.MinValue, Double.MaxValue), Gen.alphaStr)

    def genList(depth: Int): Gen[List[Any]] =
      Gen.choose(0, MaxCollectionLength).flatMap(n => Gen.listOfN(n, Gen.lzy(genAny(depth - 1))))

    def genMap(depth: Int): Gen[Map[String, Any]] =
      Gen
        .choose(0, MaxCollectionLength)
        .flatMap(n => Gen.mapOfN(n, for { k <- Gen.alphaStr; v <- Gen.lzy(genAny(depth - 1)) } yield (k, v)))

    if (depth == 0) genScalar
    else Gen.frequency(3 -> genScalar, 1 -> genList(depth).map(_.asJava), 1 -> genMap(depth).map(_.asJava))
  }

  implicit val arbConfigValue: Arbitrary[ConfigValue] =
    Arbitrary(genAny(MaxConfigDepth).map(ConfigValueFactory.fromAnyRef))

  implicit val cogenConfigValue: Cogen[ConfigValue] =
    Cogen[String].contramap[ConfigValue](_.render)

  private[this] val genFailureReason: Gen[FailureReason] =
    Gen.oneOf(
      ^^(Gen.alphaStr, Gen.alphaStr, Gen.alphaStr)(CannotConvert.apply),
      ^(Gen.posNum[Int], Gen.posNum[Int])(WrongSizeString.apply),
      ^(Gen.posNum[Int], Gen.posNum[Int])(WrongSizeList.apply),
      Gen.alphaStr.map(k => KeyNotFound.apply(k)),
      Gen.alphaStr.map(UnknownKey.apply)
    )

  implicit val arbConfigReaderFailures: Arbitrary[ConfigReaderFailures] =
    Arbitrary {
      for {
        n <- Gen.choose(1, MaxCollectionLength)
        l <- Gen.listOfN(n, genFailureReason).map(_.map(r => ConvertFailure(r, None, "")))
      } yield ConfigReaderFailures(l.head, l.tail: _*)
    }

  implicit val cogenConfigReaderFailures: Cogen[ConfigReaderFailures] =
    Cogen[String].contramap[ConfigReaderFailures](_.toString)

  implicit def arbConfigReader[A: Arbitrary]: Arbitrary[ConfigReader[A]] =
    Arbitrary(arb[ConfigValue => Either[ConfigReaderFailures, A]].map(ConfigReader.fromFunction))

  implicit def arbConfigWriter[A: Cogen]: Arbitrary[ConfigWriter[A]] =
    Arbitrary(arb[A => ConfigValue].map(ConfigWriter.fromFunction))

  implicit def arbConfigConvert[A: Arbitrary: Cogen]: Arbitrary[ConfigConvert[A]] =
    Arbitrary {
      for {
        reader <- arb[ConfigReader[A]]
        writer <- arb[ConfigWriter[A]]
      } yield ConfigConvert.fromReaderAndWriter(Derivation.Successful(reader), Derivation.Successful(writer))
    }
}
