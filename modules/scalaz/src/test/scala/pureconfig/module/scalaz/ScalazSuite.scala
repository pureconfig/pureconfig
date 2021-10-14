package pureconfig.module.scalaz

import com.typesafe.config.ConfigFactory.parseString
import scalaz.scalacheck.ScalazArbitrary._
import scalaz.std.anyVal.intInstance
import scalaz.std.string._
import scalaz.{==>>, IList, ISet, Maybe, NonEmptyList}

import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigConvertChecks}

class ScalazSuite extends BaseSuite {

  checkArbitrary[Maybe[Int]]

  checkArbitrary[IList[Int]]

  checkArbitrary[ISet[Int]]

  checkArbitrary[NonEmptyList[Int]]

  checkArbitrary[String ==>> Int]

  it should "return an EmptyIListFound when reading empty list into NonEmptyList" in {
    configValue("[]").to[NonEmptyList[Int]] should failWith(EmptyIListFound, "", stringConfigOrigin(1))
  }
}
