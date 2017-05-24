package pureconfig.module.cats

import cats.Eq
import cats.instances.either._
import cats.instances.tuple._
import cats.laws.discipline.eq._
import com.typesafe.config.ConfigValue
import org.scalacheck.Arbitrary
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.module.cats.arbitrary._
import pureconfig.module.cats.instances._

package object eq {

  implicit def configReaderEq[A: Eq]: Eq[ConfigReader[A]] =
    Eq[ConfigValue => Either[ConfigReaderFailures, A]].on[ConfigReader[A]](_.from)

  implicit def configWriterEq[A: Arbitrary]: Eq[ConfigWriter[A]] =
    Eq[A => ConfigValue].on[ConfigWriter[A]](_.to)

  implicit def configConvertEq[A: Eq: Arbitrary]: Eq[ConfigConvert[A]] =
    Eq[(ConfigReader[A], ConfigWriter[A])].on[ConfigConvert[A]] { cc => (cc, cc) }
}
