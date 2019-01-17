package pureconfig.module.cats

import cats.data._
import cats.instances.int._
import cats.instances.string._
import cats.syntax.foldable._
import com.typesafe.config.ConfigFactory.parseString
import org.scalactic.Equality
import pureconfig.generic.auto._
import pureconfig.syntax._
import pureconfig.{ BaseSuite, ConfigConvertChecks }

import scala.collection.immutable.{ SortedMap, SortedSet }

class CatsSuite extends BaseSuite with ConfigConvertChecks {

  //TODO: Should be safe to drop after Cats version >= 1.6 (https://github.com/typelevel/cats/pull/2690)
  implicit val numChainEq: Equality[NumChain] = new Equality[NumChain] {
    override def areEqual(a: NumChain, b: Any): Boolean = b match {
      case NumChain(nec) => implicitly[Equality[TraversableOnce[Int]]].areEqual(a.numbers.toList, nec.toList)
      case _ => false
    }
  }

  case class Numbers(numbers: NonEmptyList[Int])
  case class NumVec(numbers: NonEmptyVector[Int])
  case class NumSet(numbers: NonEmptySet[Int])
  case class NumMap(numbers: NonEmptyMap[String, Int])
  case class NumChain(numbers: NonEmptyChain[Int])

  checkReadWrite[Numbers](parseString(s"""{ numbers: [1,2,3] }""").root() → Numbers(NonEmptyList(1, List(2, 3))))
  checkReadWrite[NumVec](parseString(s"""{ numbers: [1,2,3] }""").root() → NumVec(NonEmptyVector(1, Vector(2, 3))))
  checkReadWrite[NumSet](parseString(s"""{ numbers: [1,2,3] }""").root() → NumSet(NonEmptySet(1, SortedSet(2, 3))))
  checkReadWrite[NumMap](parseString(s"""{ 
                                           numbers {"1": 1, "2": 2, "3": 3 }
                                         }""").root() → NumMap(NonEmptyMap(("1", 1), SortedMap("2" → 2, "3" → 3))))
  checkReadWrite[NumChain](parseString(s"""{ numbers: [1,2,3] }""").root() → NumChain(NonEmptyChain(1, 2, 3)))

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyList" in {
    val config = parseString("{ numbers: [] }")
    config.to[Numbers] should failWith(EmptyTraversableFound("scala.collection.immutable.List"), "numbers")
  }

  it should "return an EmptyTraversableFound when reading empty vector into NonEmptyVector" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumVec] should failWith(EmptyTraversableFound("scala.collection.immutable.Vector"), "numbers")
  }

  it should "return an EmptyTraversableFound when reading empty set into NonEmptySet" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumSet] should failWith(EmptyTraversableFound("scala.collection.immutable.SortedSet"), "numbers")
  }

  it should "return an EmptyTraversableFound when reading empty map into NonEmptyMap" in {
    val config = parseString("{ numbers{} }")
    config.to[NumMap] should failWith(EmptyTraversableFound("scala.collection.immutable.Map"), "numbers")
  }

  it should "return an EmptyTraversableFound when reading empty chain into NonEmptyChain" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumChain] should failWith(EmptyTraversableFound("cats.data.Chain"), "numbers")
  }
}
