package pureconfig.module.cats

import cats.instances.either._
import cats.instances.int._
import cats.instances.tuple._
import cats.instances.unit._
import cats.laws.discipline._
import com.typesafe.config.ConfigValue
import org.scalatest.{ FunSuite, Matchers }
import org.typelevel.discipline.scalatest.Discipline
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.module.cats.arbitrary._
import pureconfig.module.cats.eq._
import pureconfig.module.cats.instances._

class CatsLawsSuite extends FunSuite with Matchers with Discipline {
  checkAll("ConfigReader[Int]", MonadErrorTests[ConfigReader, ConfigReaderFailures].monadError[Int, Int, Int])
  checkAll("ConfigReader[Int]", MonadReaderTests[ConfigReader, ConfigValue].monadReader[Int, Int, Int])
  checkAll("ConfigWriter[Int]", ContravariantTests[ConfigWriter].contravariant[Int, Int, Int])
  checkAll("ConfigConvert[Int]", InvariantTests[ConfigConvert].invariant[Int, Int, Int])
}
