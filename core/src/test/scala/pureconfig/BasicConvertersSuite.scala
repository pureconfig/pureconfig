package pureconfig

import java.net.{ URI, URL }
import java.nio.file.{ Path, Paths }
import java.time._
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.config._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{ EitherValues, FlatSpec, Matchers }
import pureconfig.ConfigConvert.{ catchReadError, fromStringReader }
import pureconfig.arbitrary._
import pureconfig.data.Percentage
import pureconfig.data.instances.percentageConfigWriter
import pureconfig.error.{ CannotConvert, ConfigReaderFailures, EmptyStringFound }

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration.{ Duration, FiniteDuration }

class BasicConvertersSuite extends FlatSpec with ConfigConvertChecks with Matchers with GeneratorDrivenPropertyChecks with EitherValues {

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
    val conf = ConfigFactory.parseString("""{ v1 = 3
                                            | v2 = 4 }""".stripMargin)

    check[ConfigObject](
      ConfigValueFactory.fromMap(Map("v1" -> 3, "v2" -> 4).asJava) -> conf.root().asInstanceOf[ConfigValue])

    check[Config](
      conf -> conf.root().asInstanceOf[ConfigValue])
  }

  // override

  it should "be able to supersede the default Duration ConfigConvert with a locally defined ConfigConvert from fromString" in {
    case class ConfWithDuration(i: Duration)
    val expected = Duration(110, TimeUnit.DAYS)
    implicit val readDurationBadly = fromStringReader[Duration](catchReadError(_ => expected))
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "23 s").asJava).toConfig)(ConfigConvert[ConfWithDuration]).right.value shouldBe ConfWithDuration(expected)
  }

  it should "be able to supersede the default Duration ConfigConvert with a locally defined ConfigConvert" in {
    case class ConfWithDuration(i: Duration)
    val expected = Duration(220, TimeUnit.DAYS)
    implicit val readDurationBadly = new ConfigConvert[Duration] {
      override def from(config: ConfigValue): Either[ConfigReaderFailures, Duration] = Right(expected)
      override def to(t: Duration): ConfigValue = throw new Exception("Not Implemented")
    }
    loadConfig(ConfigValueFactory.fromMap(Map("i" -> "42 h").asJava).toConfig)(ConfigConvert[ConfWithDuration]).right.value shouldBe ConfWithDuration(expected)
  }

  it should "allow a custom ConfigConvert[URL] to override our definition" in {
    case class ConfWithURL(url: URL)
    val expected = "http://bad/horse/will?make=you&his=mare"
    implicit val readURLBadly = fromStringReader[URL](catchReadError(_ => new URL(expected)))
    val config = loadConfig[ConfWithURL](ConfigValueFactory.fromMap(Map("url" -> "https://ignored/url").asJava).toConfig)
    config.right.value.url shouldBe new URL(expected)
  }

  it should "allow a custom ConfigConvert[UUID] to override our definition" in {
    case class ConfWithUUID(uuid: UUID)
    val expected = "bcd787fe-f510-4f84-9e64-f843afd19c60"
    implicit val readUUIDBadly = fromStringReader[UUID](catchReadError(_ => UUID.fromString(expected)))
    val config = loadConfig[ConfWithUUID](ConfigValueFactory.fromMap(Map("uuid" -> "ignored").asJava).toConfig)
    config.right.value.uuid shouldBe UUID.fromString(expected)
  }

  case class ConfWithPath(myPath: Path)

  it should "allow a custom ConfigConvert[Path] to override our definition" in {
    val expected = "c:\\this\\is\\a\\custom\\path"
    implicit val readPathBadly = fromStringReader[Path](_ => _ => Right(Paths.get(expected)))
    val config = loadConfig[ConfWithPath](ConfigValueFactory.fromMap(Map("my-path" -> "/this/is/ignored").asJava).toConfig)
    config.right.value.myPath shouldBe Paths.get(expected)
  }

  it should "allow a custom ConfigConvert[URI] to override our definition" in {
    case class ConfWithURI(uri: URI)
    val expected = "http://bad/horse/will?make=you&his=mare"
    implicit val readURLBadly = fromStringReader[URI](_ => _ => Right(new URI(expected)))
    val config = loadConfig[ConfWithURI](ConfigValueFactory.fromMap(Map("uri" -> "https://ignored/url").asJava).toConfig)
    config.right.value.uri shouldBe new URI(expected)
  }
}