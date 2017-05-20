package pureconfig

import java.io.File
import java.net.{ URI, URL }
import java.nio.file.Path
import java.time._
import java.time.{ Duration => JavaDuration }
import java.util.UUID
import java.util.regex.Pattern

import com.typesafe.config._
import pureconfig.arbitrary._
import pureconfig.data.Percentage
import pureconfig.equality._
import pureconfig.error.{ CannotConvert, EmptyStringFound }
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.util.matching.Regex

class BasicConvertersSuite extends BaseSuite {

  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  behavior of "ConfigConvert"

  checkArbitrary[Duration]
  checkFailure[Duration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[Duration, CannotConvert](ConfigValueFactory.fromIterable(List(1).asJava))

  checkArbitrary[JavaDuration]
  checkFailure[JavaDuration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[JavaDuration, CannotConvert](ConfigValueFactory.fromIterable(List(1).asJava))

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

  checkArbitrary[File]

  checkArbitrary[immutable.HashSet[String]]

  checkArbitrary[immutable.List[Float]]
  checkRead[immutable.List[Int]](
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
  checkRead[immutable.Set[Int]](
    (Set(4, 5, 6), ConfigValueFactory.fromMap(Map("1" -> 4, "2" -> 5, "3" -> 6).asJava)))

  checkArbitrary[immutable.Stream[String]]

  checkArbitrary[immutable.TreeSet[Int]]

  checkArbitrary[immutable.Vector[Short]]

  checkArbitrary[Option[Int]]

  checkRead[Pattern](Pattern.compile("(a|b)") -> ConfigValueFactory.fromAnyRef("(a|b)"))

  checkRead[Regex](new Regex("(a|b)") -> ConfigValueFactory.fromAnyRef("(a|b)"))

  checkFailure[Pattern, CannotConvert](ConfigValueFactory.fromAnyRef("(a|b")) // missing closing ')'
  checkFailure[Regex, CannotConvert](ConfigValueFactory.fromAnyRef("(a|b")) // missing closing ')'

  checkRead[URL](
    new URL("http://host/path?with=query&param") -> ConfigValueFactory.fromAnyRef("http://host/path?with=query&param"))

  checkRead[URI](
    new URI("http://host/path?with=query&param") -> ConfigValueFactory.fromAnyRef("http://host/path?with=query&param"))

  checkRead[ConfigList](
    ConfigValueFactory.fromIterable(List().asJava) -> ConfigValueFactory.fromIterable(List().asJava),
    ConfigValueFactory.fromIterable(List(1, 2, 3).asJava) -> ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))

  checkRead[ConfigValue](
    ConfigValueFactory.fromAnyRef(4) -> ConfigValueFactory.fromAnyRef(4),
    ConfigValueFactory.fromAnyRef("str") -> ConfigValueFactory.fromAnyRef("str"),
    ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava) -> ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))

  {
    val conf = ConfigFactory.parseString("""{ v1 = 3, v2 = 4 }""".stripMargin)

    checkRead[ConfigObject](
      ConfigValueFactory.fromMap(Map("v1" -> 3, "v2" -> 4).asJava) -> conf.root().asInstanceOf[ConfigValue])

    checkRead[Config](
      conf -> conf.root().asInstanceOf[ConfigValue])
  }
}
