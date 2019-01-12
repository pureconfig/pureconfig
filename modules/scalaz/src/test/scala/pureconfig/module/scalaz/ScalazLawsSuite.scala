package pureconfig.module.scalaz

import com.typesafe.config.ConfigValue
import org.scalacheck.{ Prop, Properties }
import org.scalatest.prop.Checkers
import org.scalatest.FunSuite
import pureconfig.{ ConfigConvert, ConfigReader, ConfigWriter }
import pureconfig.error.ConfigReaderFailures
import pureconfig.module.scalaz.arbitrary._
import pureconfig.module.scalaz.equal._
import pureconfig.module.scalaz.instances._

import scalaz.scalacheck.ScalazProperties._
import scalaz.std.anyVal.intInstance

class ScalazLawsSuite extends FunSuite with Checkers {
  import ScalazLawsSuite._

  test("contravariant.laws[ConfigWriter]") {
    check(properties2prop(contravariant.laws[ConfigWriter]))
  }

  test("invariantFunctor.laws[ConfigConvert]") {
    check(properties2prop(invariantFunctor.laws[ConfigConvert]))
  }

  test("monadError.laws[ConfigReader, ConfigReaderFailures]") {
    check(properties2prop(monadError.laws[ConfigReader, ConfigReaderFailures]))
  }

  test("semigroup.laws[ConfigReaderFailures]") {
    check(properties2prop(semigroup.laws[ConfigReaderFailures]))
  }

  test("semigroup.laws[ConfigValue]") {
    check(properties2prop(semigroup.laws[ConfigValue]))
  }
}

object ScalazLawsSuite {
  def properties2prop(ps: Properties): Prop = Prop.all(ps.properties.map(_._2): _*)
}
