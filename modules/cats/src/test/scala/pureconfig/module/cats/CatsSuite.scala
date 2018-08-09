package pureconfig.module.cats

import cats.data.{ NonEmptyList, NonEmptyMap, NonEmptySet, NonEmptyVector }
import cats.instances.int._
import cats.instances.string._
import com.typesafe.config.ConfigFactory.parseString
import pureconfig.syntax._
import pureconfig.{ BaseSuite, ConfigConvertChecks }

import scala.collection.immutable.{ SortedMap, SortedSet }

class CatsSuite extends BaseSuite with ConfigConvertChecks {

  case class Numbers(numbers: NonEmptyList[Int])
  case class NumVec(numbers: NonEmptyVector[Int])
  case class NumSet(numbers: NonEmptySet[Int])
  case class NumMap(numbers: NonEmptyMap[String, Int])

  checkReadWrite[Numbers](parseString(s"""{ numbers: [1,2,3] }""").root() → Numbers(NonEmptyList(1, List(2, 3))))
  checkReadWrite[NumVec](parseString(s"""{ numbers: [1,2,3] }""").root() → NumVec(NonEmptyVector(1, Vector(2, 3))))
  checkReadWrite[NumSet](parseString(s"""{ numbers: [1,2,3] }""").root() → NumSet(NonEmptySet(1, SortedSet(2, 3))))
  checkReadWrite[NumMap](
    parseString(s"""{ numbers { "1": 1, "2": 2, "3": 3 } }""").root() → NumMap(NonEmptyMap(("1", 1), SortedMap("2" → 2, "3" → 3))))

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyList" in {
    val config = parseString("{ numbers: [] }")
    config.to[Numbers] should failWith(EmptyTraversableFound("scala.collection.immutable.List"), "numbers")
  }

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyVector" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumVec] should failWith(EmptyTraversableFound("scala.collection.immutable.Vector"), "numbers")
  }

  it should "return an EmptyTraversableFound when reading empty set into NonEmptySet" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumSet] should failWith(EmptyTraversableFound("scala.collection.immutable.SortedSet"), "numbers")
  }

  it should "return an EmptyTraversableFound when reading empty set into NonEmptyMap" in {
    val config = parseString("{ numbers{} }")
    config.to[NumMap] should failWith(EmptyTraversableFound("scala.collection.immutable.Map"), "numbers")
  }
}
