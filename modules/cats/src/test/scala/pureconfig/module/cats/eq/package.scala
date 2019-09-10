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

  implicit def configReaderEq[A: Eq](implicit arbCV: Arbitrary[ConfigValue]): Eq[ConfigReader[A]] = new Eq[ConfigReader[A]] {
    val nSamples = 50
    val resultEq = Eq[ConfigReader.Result[A]]

    def eqv(x: ConfigReader[A], y: ConfigReader[A]): Boolean = {
      val samples = List.fill(nSamples)(arbCV.arbitrary.sample).collect {
        case Some(a) => a
        case None => sys.error("Could not generate arbitrary values to compare two ConfigReaders")
      }

      samples.forall(s => resultEq.eqv(x.from(s), y.from(s)))
    }
  }

  implicit def configWriterEq[A](implicit arbA: Arbitrary[A]): Eq[ConfigWriter[A]] = new Eq[ConfigWriter[A]] {
    val nSamples = 50
    val resultEq = Eq[ConfigValue]

    def eqv(x: ConfigWriter[A], y: ConfigWriter[A]): Boolean = {
      val samples = List.fill(nSamples)(arbA.arbitrary.sample).collect {
        case Some(a) => a
        case None => sys.error("Could not generate arbitrary values to compare two ConfigWriters")
      }

      samples.forall(s => resultEq.eqv(x.to(s), y.to(s)))
    }
  }

  implicit def configConvertEq[A: Eq: Arbitrary]: Eq[ConfigConvert[A]] =
    Eq.by[ConfigConvert[A], (ConfigReader[A], ConfigWriter[A])] { cc => (cc, cc) }
}
