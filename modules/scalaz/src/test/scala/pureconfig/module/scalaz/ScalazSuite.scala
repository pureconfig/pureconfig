package pureconfig.module.scalaz

import com.typesafe.config.ConfigFactory.parseString
import scalaz.scalacheck.ScalazArbitrary._
import scalaz.std.anyVal.intInstance
import scalaz.std.string._
import scalaz.{==>>, IList, ISet, Maybe, NonEmptyList}

import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigConvertChecks, ConfigSource}

class ScalazSuite extends BaseSuite {

  checkArbitrary[Maybe[Int]]

  checkArbitrary[IList[Int]]

  checkArbitrary[ISet[Int]]

  checkArbitrary[NonEmptyList[Int]]

  checkArbitrary[String ==>> Int]

  it should "return an EmptyIListFound when reading empty list into NonEmptyList" in {
    val source = ConfigSource.string("{ numbers: [] }")
    source.at("numbers").load[NonEmptyList[Int]] should failWith(EmptyIListFound, "numbers", stringConfigOrigin(1))
  }
}
