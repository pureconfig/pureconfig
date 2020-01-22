package pureconfig.module.cats

import cats.instances.either._
import cats.instances.int._
import cats.instances.tuple._
import cats.instances.unit._
import cats.kernel.laws.discipline.{ MonoidTests, SemigroupTests }
import cats.laws.discipline._
import com.typesafe.config.{ Config, ConfigValue }
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.typelevel.discipline.scalatest.FunSuiteDiscipline
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.module.cats.arbitrary._
import pureconfig.module.cats.eq._
import pureconfig.module.cats.instances._

class CatsLawsSuite extends AnyFunSuite with ScalaCheckDrivenPropertyChecks with FunSuiteDiscipline {
  checkAll("ConfigReader[Int]", ApplicativeErrorTests[ConfigReader, ConfigReaderFailures].applicativeError[Int, Int, Int])
  checkAll("ConfigWriter[Int]", ContravariantSemigroupalTests[ConfigWriter].contravariantSemigroupal[Int, Int, Int])
  checkAll("ConfigConvert[Int]", InvariantSemigroupalTests[ConfigConvert].invariantSemigroupal[Int, Int, Int])

  checkAll("ConfigValue", SemigroupTests[ConfigValue].semigroup)
  checkAll("Config", MonoidTests[Config].monoid)
  checkAll("ConfigReaderFailures", SemigroupTests[ConfigReaderFailures].semigroup)
  checkAll("ConfigObjectSource", MonoidTests[ConfigObjectSource].monoid)
}
