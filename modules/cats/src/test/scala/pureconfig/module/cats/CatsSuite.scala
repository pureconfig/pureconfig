package pureconfig.module.cats

import cats.data.{ NonEmptyList, NonEmptyVector }
import com.typesafe.config.ConfigFactory.parseString
import org.scalatest._
import pureconfig.error.ConfigReaderFailures
import pureconfig.module.Cats.EmptyTraversableFound
import pureconfig.syntax._

class CatsSuite extends FlatSpec with Matchers with EitherValues {

  case class Numbers(numbers: NonEmptyList[Int])

  it should "be able to read a config with a NonEmptyList" in {
    val config = parseString(s"""{ numbers: [1,2,3] }""")
    config.to[Numbers] shouldEqual Right(Numbers(NonEmptyList(1, List(2, 3))))
  }

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyList" in {
    val config = parseString("{ numbers: [] }")
    config.to[Numbers] shouldEqual
      Left(ConfigReaderFailures(EmptyTraversableFound("scala.collection.immutable.List", None, Some("numbers")), Nil))
  }

  case class NumVec(numbers: NonEmptyVector[Int])

  it should "be able to read a config with an NonEmptyVector" in {
    val config = parseString(s"""{ numbers: [1,2,3] }""")
    config.to[NumVec] shouldEqual Right(NumVec(NonEmptyVector(1, Vector(2, 3))))
  }

  it should "return an EmptyTraversableFound when reading empty lists into NonEmptyVector" in {
    val config = parseString("{ numbers: [] }")
    config.to[NumVec] shouldEqual
      Left(ConfigReaderFailures(EmptyTraversableFound("scala.collection.immutable.Vector", None, Some("numbers")), Nil))
  }
}
