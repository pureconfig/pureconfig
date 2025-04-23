package pureconfig

import scala.collection.immutable.{HashSet, ListSet, Queue, TreeSet}
import scala.jdk.CollectionConverters._

import com.typesafe.config.{ConfigFactory, ConfigValueFactory, ConfigValueType}

import pureconfig.error.{ConfigReaderFailures, ConvertFailure, WrongType}

class CollectionConvertersSuite extends BaseSuite {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

  behavior of "ConfigConvert"

  checkArbitrary[HashSet[String]]

  checkArbitrary[List[Float]]
  checkRead[List[Int]](
    // order of keys maintained
    ConfigValueFactory.fromMap(Map("2" -> 1, "0" -> 2, "1" -> 3).asJava) -> List(2, 3, 1),
    ConfigValueFactory.fromMap(Map("3" -> 2, "1" -> 4).asJava) -> List(4, 2),
    ConfigValueFactory.fromMap(Map("1" -> 1, "a" -> 2).asJava) -> List(1)
  )

  checkFailures[List[Int]](
    ConfigValueFactory.fromMap(Map("b" -> 1, "a" -> 2).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST)), emptyConfigOrigin, "")
    ),
    ConfigValueFactory.fromMap(Map().asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.LIST)), emptyConfigOrigin, "")
    )
  )

  checkArbitrary[ListSet[Int]]

  checkArbitrary[Map[String, Int]]
  checkFailures[Map[String, Int]](
    // nested map should fail
    ConfigFactory.parseString("conf.a=1").root() -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.OBJECT, Set(ConfigValueType.NUMBER)), stringConfigOrigin(1), "conf")
    ),
    // wrong value type should fail
    ConfigFactory.parseString("{ a=b }").root() -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), stringConfigOrigin(1), "a")
    )
  )

  checkArbitrary[Queue[Boolean]]

  checkArbitrary[Set[Double]]
  checkRead[Set[Int]](ConfigValueFactory.fromMap(Map("1" -> 4, "2" -> 5, "3" -> 6).asJava) -> Set(4, 5, 6))

  checkArbitrary[LazyList[String]]

  checkArbitrary[TreeSet[Int]]

  checkArbitrary[Vector[Short]]

  checkArbitrary[Option[Int]]

  checkArbitrary[Array[Int]]
}
