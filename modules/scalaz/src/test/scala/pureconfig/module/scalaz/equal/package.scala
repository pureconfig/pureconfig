package pureconfig.module.scalaz

import com.typesafe.config.ConfigValue
import org.scalacheck.{Arbitrary, Gen}
import scalaz.Equal
import scalaz.std.either._
import scalaz.std.tuple._

import pureconfig.module.scalaz.arbitrary._
import pureconfig.module.scalaz.instances._
import pureconfig.{ConfigConvert, ConfigReader, ConfigWriter}

package object equal {

  implicit def function1Equal[A, B](implicit arbitraryA: Arbitrary[A], equalB: Equal[B]): Equal[A => B] =
    new Equal[A => B] {
      private val samplesCount: Int = 50

      def equal(f: A => B, g: A => B): Boolean = {
        val samples = Gen.listOfN(samplesCount, arbitraryA.arbitrary).sample match {
          case Some(lst) => lst
          case None => sys.error("Could not generate arbitrary values to compare two functions")
        }

        samples.forall(s => equalB.equal(f(s), g(s)))
      }
    }

  implicit def configReaderEqual[A: Equal]: Equal[ConfigReader[A]] =
    Equal.equalBy[ConfigReader[A], ConfigValue => ConfigReader.Result[A]](_.from)

  implicit def configWriterEqual[A: Arbitrary]: Equal[ConfigWriter[A]] =
    Equal.equalBy[ConfigWriter[A], A => ConfigValue](_.to)

  implicit def configConvertEqual[A: Equal: Arbitrary]: Equal[ConfigConvert[A]] =
    Equal.equalBy[ConfigConvert[A], (ConfigReader[A], ConfigWriter[A])] { cc => (cc, cc) }
}
