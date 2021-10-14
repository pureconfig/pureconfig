package pureconfig.module.cats

import scala.collection.immutable.{SortedMap, SortedSet}

import cats.data._
import cats.implicits._

import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigReader, ConfigWriter}

class CatsSuite extends BaseSuite {

  // This wrapper is helpful for the checkReadWrite calls below due to the fact that some of types used in this suite
  // don't have neither a TypeTag nor a ClassTag (see https://github.com/typelevel/cats/issues/2928).
  case class Numbers[T](numbers: T)
  object Numbers {
    implicit def reader[T: ConfigReader]: ConfigReader[Numbers[T]] =
      ConfigReader[T].map(Numbers.apply)
    implicit def writer[T: ConfigWriter]: ConfigWriter[Numbers[T]] =
      ConfigWriter[T].contramap(_.numbers)
  }

  checkReadWrite[Numbers[NonEmptyList[Int]]](configValue("[1, 2, 3]") -> Numbers(NonEmptyList(1, List(2, 3))))
  checkReadWrite[Numbers[NonEmptyVector[Int]]](configValue("[1, 2, 3]") -> Numbers(NonEmptyVector(1, Vector(2, 3))))
  checkReadWrite[Numbers[NonEmptySet[Int]]](configValue("[1, 2, 3]") -> Numbers(NonEmptySet(1, SortedSet(2, 3))))
  checkReadWrite[Numbers[NonEmptyMap[String, Int]]](
    configValue("""{"1": 1, "2": 2, "3": 3 }""") -> Numbers(NonEmptyMap(("1", 1), SortedMap("2" -> 2, "3" -> 3)))
  )
  checkReadWrite[Numbers[NonEmptyChain[Int]]](configValue("[1, 2, 3]") -> Numbers(NonEmptyChain(1, 2, 3)))

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyList" in {
    configValue("[]").to[NonEmptyList[Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.List"),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty vector into NonEmptyVector" in {
    configValue("[]").to[NonEmptyVector[Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.Vector"),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty set into NonEmptySet" in {
    configValue("[]").to[NonEmptySet[Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.SortedSet"),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty map into NonEmptyMap" in {
    configValue("{}").to[NonEmptyMap[String, Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.Map"),
      "",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty chain into NonEmptyChain" in {
    configValue("[]").to[NonEmptyChain[Int]] should failWith(
      EmptyTraversableFound("cats.data.Chain"),
      "",
      stringConfigOrigin(1)
    )
  }
}
