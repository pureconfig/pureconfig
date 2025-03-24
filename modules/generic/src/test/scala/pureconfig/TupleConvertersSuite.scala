package pureconfig

import scala.jdk.CollectionConverters._

import com.typesafe.config.{ConfigValueFactory, ConfigValueType}
import org.scalacheck.Arbitrary

import pureconfig.error._
import pureconfig.generic.auto._

class TupleConvertersSuite extends BaseSuite {
  case class Foo(a: Int, b: String)

  implicit val arbFoo: Arbitrary[Foo] = Arbitrary {
    Arbitrary.arbitrary[(Int, String)].map((Foo.apply _).tupled)
  }

  behavior of "ConfigConvert"

  // Check arbitrary Tuples
  checkArbitrary[Tuple1[Int]]
  checkArbitrary[(String, Int)]
  checkArbitrary[(Int, (Long, String), Boolean)]

  // Check arbitrary Tuples with custom types
  checkArbitrary[(Long, Foo, Boolean, Foo)]

  // Check readers from objects and lists
  checkRead[(String, Int)](ConfigValueFactory.fromMap(Map("_1" -> "one", "_2" -> 2).asJava) -> (("one", 2)))
  checkRead[(String, Int)](ConfigValueFactory.fromMap(Map("0" -> "one", "1" -> 2).asJava) -> (("one", 2)))
  checkRead[(String, Int)](ConfigValueFactory.fromIterable(List("one", 2).asJava) -> (("one", 2)))

  // Check writers
  checkWrite[Tuple1[Int]](Tuple1(1) -> ConfigValueFactory.fromIterable(List(1).asJava))
  checkWrite[(String, Int)](("one", 2) -> ConfigValueFactory.fromIterable(List("one", 2).asJava))
  checkWrite[(Int, (Long, String), Boolean)](
    (1, (2L, "three"), false) -> ConfigValueFactory.fromIterable(List(1, List(2L, "three").asJava, false).asJava)
  )

  // Check errors
  checkFailures[(String, Int)](
    ConfigValueFactory.fromAnyRef(Map("_1" -> "one", "_2" -> "two").asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), emptyConfigOrigin, "_2")
    )
  )
  checkFailures[(String, Int)](
    ConfigValueFactory.fromIterable(List("one", "two").asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), emptyConfigOrigin, "1")
    )
  )

  checkFailures[(Int, Int, Int)](
    ConfigValueFactory.fromIterable(List(1, "one").asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongSizeList(3, 2), emptyConfigOrigin, "")
    )
  )

  checkFailures[(Int, Int, Int)](
    ConfigValueFactory.fromAnyRef(Map("_1" -> "one", "_2" -> 2).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.NUMBER)), emptyConfigOrigin, "_1"),
      ConvertFailure(KeyNotFound("_3", Set()), emptyConfigOrigin, "")
    )
  )

  checkFailures[(String, Int)](
    ConfigValueFactory.fromAnyRef("str") -> ConfigReaderFailures(
      ConvertFailure(WrongType(ConfigValueType.STRING, Set(ConfigValueType.LIST)), emptyConfigOrigin, "")
    )
  )
}
