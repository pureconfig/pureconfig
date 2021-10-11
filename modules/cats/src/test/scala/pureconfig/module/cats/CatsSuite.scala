package pureconfig.module.cats

import scala.collection.immutable.{SortedMap, SortedSet}

import cats.data._
import cats.implicits._
import com.typesafe.config.ConfigFactory.parseString

import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigReader, ConfigSource, ConfigWriter}

class CatsSuite extends BaseSuite {

  case class Numbers[T](numbers: T)
  object Numbers {
    implicit def reader[T: ConfigReader]: ConfigReader[Numbers[T]] =
      ConfigReader.forProduct1("numbers")(Numbers.apply)
    implicit def writer[T: ConfigWriter]: ConfigWriter[Numbers[T]] =
      ConfigWriter.forProduct1("numbers")(_.numbers)
  }

  checkReadWrite[Numbers[NonEmptyList[Int]]](
    parseString(s"""{ numbers: [1, 2, 3] }""").root() -> Numbers(NonEmptyList(1, List(2, 3)))
  )
  checkReadWrite[Numbers[NonEmptyVector[Int]]](
    parseString(s"""{ numbers: [1, 2, 3] }""").root() -> Numbers(NonEmptyVector(1, Vector(2, 3)))
  )
  checkReadWrite[Numbers[NonEmptySet[Int]]](
    parseString(s"""{ numbers: [1, 2, 3] }""").root() -> Numbers(NonEmptySet(1, SortedSet(2, 3)))
  )
  checkReadWrite[Numbers[NonEmptyMap[String, Int]]](
    parseString(s"""{ numbers {"1": 1, "2": 2, "3": 3 } }""").root() -> Numbers(
      NonEmptyMap(("1", 1), SortedMap("2" -> 2, "3" -> 3))
    )
  )
  checkReadWrite[Numbers[NonEmptyChain[Int]]](
    parseString(s"""{ numbers: [1, 2, 3] }""").root() -> Numbers(NonEmptyChain(1, 2, 3))
  )

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyList" in {
    val source = ConfigSource.string("{ numbers: [] }")
    source.at("numbers").load[NonEmptyList[Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.List"),
      "numbers",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty vector into NonEmptyVector" in {
    val source = ConfigSource.string("{ numbers: [] }")
    source.at("numbers").load[NonEmptyVector[Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.Vector"),
      "numbers",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty set into NonEmptySet" in {
    val source = ConfigSource.string("{ numbers: [] }")
    source.at("numbers").load[NonEmptySet[Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.SortedSet"),
      "numbers",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty map into NonEmptyMap" in {
    val source = ConfigSource.string("{ numbers{} }")
    source.at("numbers").load[NonEmptyMap[String, Int]] should failWith(
      EmptyTraversableFound("scala.collection.immutable.Map"),
      "numbers",
      stringConfigOrigin(1)
    )
  }

  it should "return an EmptyTraversableFound when reading empty chain into NonEmptyChain" in {
    val source = ConfigSource.string("{ numbers: [] }")
    source.at("numbers").load[NonEmptyChain[Int]] should failWith(
      EmptyTraversableFound("cats.data.Chain"),
      "numbers",
      stringConfigOrigin(1)
    )
  }
}
