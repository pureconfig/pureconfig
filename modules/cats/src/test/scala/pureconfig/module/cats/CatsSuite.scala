package pureconfig.module.cats

import cats.data.{ NonEmptyList, NonEmptyVector }
import com.typesafe.config.ConfigFactory
import org.scalatest._
import pureconfig.syntax._

class CatsSuite extends FlatSpec with Matchers with EitherValues {

  case class Numbers(numbers: NonEmptyList[Int])

  it should "be able to read a config with a NonEmptyList" in {
    val config = ConfigFactory.parseString(s"""{ numbers: [1,2,3] }""")
    config.to[Numbers].right.value shouldEqual Numbers(NonEmptyList(1, List(2, 3)))
  }

  case class NumVec(numbers: NonEmptyVector[Int])

  it should "be able to read a config with an NonEmptyVector" in {
    val config = ConfigFactory.parseString(s"""{ numbers: [1,2,3] }""")
    config.to[NumVec].right.value shouldEqual NumVec(NonEmptyVector(1, Vector(2, 3)))
  }
}
