package pureconfig.module.cats

import cats.Eq
import cats.instances.either._
import cats.instances.tuple._
import cats.laws.discipline.eq._
import com.typesafe.config.ConfigValue
import org.scalacheck.Arbitrary
import pureconfig._
import pureconfig.module.cats.arbitrary._
import pureconfig.module.cats.instances._

package object eq {

  implicit def configReaderEq[A: Eq]: Eq[ConfigReader[A]] =
    Eq.by[ConfigReader[A], ConfigValue => ConfigReader.Result[A]](_.from)

  implicit def configWriterEq[A: Arbitrary]: Eq[ConfigWriter[A]] =
    Eq.by[ConfigWriter[A], A => ConfigValue](_.to)

  implicit def configConvertEq[A: Eq: Arbitrary]: Eq[ConfigConvert[A]] =
    Eq.by[ConfigConvert[A], (ConfigReader[A], ConfigWriter[A])] { cc => (cc, cc) }
}
