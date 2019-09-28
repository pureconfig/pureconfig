package pureconfig.module.cats

import cats.data._
import cats.implicits._
import com.typesafe.config.ConfigFactory.parseString
import pureconfig.generic.auto._
import pureconfig.syntax._
import pureconfig.{ BaseSuite, ConfigConvertChecks, ConfigReader, ConfigWriter }
import scala.collection.immutable.{ SortedMap, SortedSet }

import com.typesafe.config.{ ConfigRenderOptions, ConfigValue }
import org.scalatest.flatspec.AnyFlatSpec
import scala.reflect.runtime.universe._

import org.scalactic.Equality
import org.scalatest.matchers.Matcher
import org.scalatest.{ EitherValues, Matchers }
import pureconfig.error.{ ConfigReaderFailures, ConfigValueLocation, ConvertFailure, FailureReason }

class CatsSuite extends AnyFlatSpec with Matchers with EitherValues {

  // `discipline-scalatest` currently depends on a snapshot version of ScalaTest which doesn't provide FlatSpec. We have
  // the following checks under `ConfigConvertChecks` of our `tests` package, which requires types extending it to
  // extend from `FlatSpec`. Since we can't extend `ConfigConvertChecks` here, the relevant checks are copied for
  // convenience. We should be able to remove them once a stable version of `discipline-scalatest` is released and we
  // update ScalaTest.
  def checkRead[T: Equality](reprsToValues: (ConfigValue, T)*)(implicit cr: ConfigReader[T], tpe: TypeTag[T]): Unit =
    for ((repr, value) <- reprsToValues) {
      it should s"read the value $value of type ${tpe.tpe} from ${repr.render(ConfigRenderOptions.concise())}" in {
        cr.from(repr).right.value shouldEqual value
      }
    }

  def checkWrite[T: Equality](valuesToReprs: (T, ConfigValue)*)(implicit cw: ConfigWriter[T], tpe: TypeTag[T]): Unit =
    for ((value, repr) <- valuesToReprs) {
      it should s"write the value $value of type ${tpe.tpe} to ${repr.render(ConfigRenderOptions.concise())}" in {
        cw.to(value) shouldEqual repr
      }
    }

  def checkReadWrite[T: ConfigReader: ConfigWriter: TypeTag: Equality](reprsValues: (ConfigValue, T)*): Unit = {
    checkRead[T](reprsValues: _*)
    checkWrite[T](reprsValues.map(_.swap): _*)
  }

  def failWith(
    reason: FailureReason,
    path: String,
    location: Option[ConfigValueLocation] = None): Matcher[ConfigReader.Result[Any]] =
    be(Left(ConfigReaderFailures(ConvertFailure(reason, location, path), Nil)))

  case class Numbers(numbers: NonEmptyList[Int])
  case class NumVec(numbers: NonEmptyVector[Int])
  case class NumSet(numbers: NonEmptySet[Int])
  case class NumMap(numbers: NonEmptyMap[String, Int])
  case class NumChain(numbers: NonEmptyChain[Int])

  checkReadWrite[Numbers](parseString(s"""{ numbers: [1,2,3] }""").root() -> Numbers(NonEmptyList(1, List(2, 3))))
  checkReadWrite[NumVec](parseString(s"""{ numbers: [1,2,3] }""").root() -> NumVec(NonEmptyVector(1, Vector(2, 3))))
  checkReadWrite[NumSet](parseString(s"""{ numbers: [1,2,3] }""").root() -> NumSet(NonEmptySet(1, SortedSet(2, 3))))
  checkReadWrite[NumMap](parseString(s"""{
                                           numbers {"1": 1, "2": 2, "3": 3 }
                                         }""").root() -> NumMap(NonEmptyMap(("1", 1), SortedMap("2" -> 2, "3" -> 3))))
  checkReadWrite[NumChain](parseString(s"""{ numbers: [1,2,3] }""").root() -> NumChain(NonEmptyChain(1, 2, 3)))

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
