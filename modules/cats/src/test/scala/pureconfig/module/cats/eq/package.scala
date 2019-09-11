package pureconfig.module.cats

import cats.Eq
import cats.instances.either._
import cats.instances.tuple._
import com.typesafe.config.ConfigValue
import org.scalacheck.Arbitrary
import pureconfig._
import pureconfig.module.cats.arbitrary._
import pureconfig.module.cats.instances._

package object eq {

  // This is the old implementation (pre-2.0.0) of Eq for functions in Cats. The new implementation requires an instance
  // of ExhaustiveCheck (see https://github.com/typelevel/cats/pull/2577), which we are unable to provide for the types
  // we use in tests. For our use case, it's OK to go with Arbitrary values.
  private implicit def catsLawsEqForFn1[A, B](implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] = new Eq[A => B] {
    val sampleCnt: Int = 50

    def eqv(f: A => B, g: A => B): Boolean = {
      val samples = List.fill(sampleCnt)(A.arbitrary.sample).collect {
        case Some(a) => a
        case None => sys.error("Could not generate arbitrary values to compare two functions")
      }
      samples.forall(s => B.eqv(f(s), g(s)))
    }
  }

  implicit def configReaderEq[A: Eq]: Eq[ConfigReader[A]] =
    Eq.by[ConfigReader[A], ConfigValue => ConfigReader.Result[A]](_.from)

  implicit def configWriterEq[A: Arbitrary]: Eq[ConfigWriter[A]] =
    Eq.by[ConfigWriter[A], A => ConfigValue](_.to)

  implicit def configConvertEq[A: Eq: Arbitrary]: Eq[ConfigConvert[A]] =
    Eq.by[ConfigConvert[A], (ConfigReader[A], ConfigWriter[A])] { cc => (cc, cc) }
}
