package pureconfig.module.cats

import cats.data.{ NonEmptyList, NonEmptySet, NonEmptyVector }
import cats.instances.int._
import com.typesafe.config.ConfigFactory.parseString
import pureconfig.BaseSuite
import pureconfig.syntax._

import scala.collection.immutable.SortedSet

class CatsSuite extends BaseSuite {

  case class Numbers(numbers: NonEmptyList[Int])

  it should "be able to read a config with a NonEmptyList" in {
    val config = parseString(s"""{ numbers: [1,2,3] }""")
    config.to[Numbers] shouldEqual Right(Numbers(NonEmptyList(1, List(2, 3))))
  }

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyList" in {
    val config = parseString("{ numbers: [] }")
    config.to[Numbers] should failWith(EmptyTraversableFound("scala.collection.immutable.List"), "numbers")
  }

  case class NumVec(numbers: NonEmptyVector[Int])

  it should "be able to read a config with an NonEmptyVector" in {
    val config = parseString(s"""{ numbers: [1,2,3] }""")
    config.to[NumVec] shouldEqual Right(NumVec(NonEmptyVector(1, Vector(2, 3))))
  }

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyVector" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumVec] should failWith(EmptyTraversableFound("scala.collection.immutable.Vector"), "numbers")
  }

  case class NumSet(numbers: NonEmptySet[Int])

  it should "be able to read a config with an NonEmptySet" in {
    val config = parseString(s"""{ numbers: [1,2,3] }""")
    config.to[NumSet] shouldEqual Right(NumSet(NonEmptySet(1, SortedSet(2, 3))))
  }

  it should "return an EmptyTraversableFound when reading empty set into NonEmptySet" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumSet] should failWith(EmptyTraversableFound("scala.collection.immutable.SortedSet"), "numbers")
  }
}
