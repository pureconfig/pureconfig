package pureconfig

import java.net.{ URI, URL }
import java.nio.file.Path
import java.time._
import java.util.UUID

import com.typesafe.config._
import pureconfig.arbitrary._
import pureconfig.data.Percentage
import pureconfig.error.{ CannotConvert, EmptyStringFound }

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration.{ Duration, FiniteDuration }

class BasicConvertersSuite extends BaseSuite {

  behavior of "ConfigConvert"

  checkArbitrary[Duration]
  checkFailure[Duration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[Duration, CannotConvert](ConfigValueFactory.fromIterable(List(1).asJava))

  checkArbitrary[FiniteDuration]
  checkFailure[FiniteDuration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[FiniteDuration, CannotConvert](
    ConfigValueFactory.fromIterable(List(1).asJava),
    ConfigConvert[Duration].to(Duration.MinusInf),
    ConfigConvert[Duration].to(Duration.Inf))

  checkArbitrary[Instant]

  checkArbitrary[ZoneOffset]

  checkArbitrary[ZoneId]

  checkArbitrary[Period]

  checkArbitrary[Year]

  checkArbitrary[String]

  checkArbitrary[Boolean]

  checkArbitrary[Double]
  checkArbitrary2[Double, Percentage](_.toDoubleFraction)
  checkFailure[Double, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[Double, CannotConvert](ConfigValueFactory.fromIterable(List(1, 2, 3, 4).asJava))

  checkArbitrary[Float]
  checkArbitrary2[Float, Percentage](_.toFloatFraction)

  checkArbitrary[Int]

  checkArbitrary[Long]

  checkArbitrary[Short]

  checkArbitrary[UUID]

  checkArbitrary[Path]

  checkArbitrary[immutable.HashSet[String]]

  checkArbitrary[immutable.List[Float]]
  check[immutable.List[Int]](
    // order of keys maintained
    (List(2, 3, 1), ConfigValueFactory.fromMap(Map("2" -> 1, "0" -> 2, "1" -> 3).asJava)),
    (List(4, 2), ConfigValueFactory.fromMap(Map("3" -> 2, "1" -> 4).asJava)))
  checkFailure[immutable.List[Int], CannotConvert](
    ConfigValueFactory.fromMap(Map("1" -> 1, "a" -> 2).asJava))

  checkArbitrary[immutable.ListSet[Int]]

  checkArbitrary[immutable.Map[String, Int]]
  checkFailure[immutable.Map[String, Int], CannotConvert](
    ConfigFactory.parseString("conf.a=1").root(), // nested map should fail
    ConfigFactory.parseString("{ a=b }").root()) // wrong value type should fail

  checkArbitrary[immutable.Queue[Boolean]]

  checkArbitrary[immutable.Set[Double]]
  check[immutable.Set[Int]](
    (Set(4, 5, 6), ConfigValueFactory.fromMap(Map("1" -> 4, "2" -> 5, "3" -> 6).asJava)))

  checkArbitrary[immutable.Stream[String]]

  checkArbitrary[immutable.TreeSet[Int]]

  checkArbitrary[immutable.Vector[Short]]

  checkArbitrary[Option[Int]]

  check[URL](
    new URL("http://host/path?with=query&param") -> ConfigValueFactory.fromAnyRef("http://host/path?with=query&param"))

  check[URI](
    new URI("http://host/path?with=query&param") -> ConfigValueFactory.fromAnyRef("http://host/path?with=query&param"))

  check[ConfigList](
    ConfigValueFactory.fromIterable(List().asJava) -> ConfigValueFactory.fromIterable(List().asJava),
    ConfigValueFactory.fromIterable(List(1, 2, 3).asJava) -> ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))

  check[ConfigValue](
    ConfigValueFactory.fromAnyRef(4) -> ConfigValueFactory.fromAnyRef(4),
    ConfigValueFactory.fromAnyRef("str") -> ConfigValueFactory.fromAnyRef("str"),
    ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava) -> ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))

  {
    val conf = ConfigFactory.parseString("""{ v1 = 3, v2 = 4 }""".stripMargin)

    check[ConfigObject](
      ConfigValueFactory.fromMap(Map("v1" -> 3, "v2" -> 4).asJava) -> conf.root().asInstanceOf[ConfigValue])

    check[Config](
      conf -> conf.root().asInstanceOf[ConfigValue])
  }
}