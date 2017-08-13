package pureconfig

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigValueFactory
import org.scalacheck.Shapeless._

import pureconfig.error._

class TupleConvertersSuite extends BaseSuite {

  behavior of "ConfigConvert"

  // Check arbitrary Tuples
  checkArbitrary[Tuple1[Int]]
  checkArbitrary[(String, Int)]
  checkArbitrary[(Int, (Long, String), Boolean)]

  // Check arbitrary Tuples with custom types
  case class Foo(a: Int, b: String)
  checkArbitrary[(Long, Foo, Boolean, Foo)]

  // Check readers from objects and lists
  checkRead[(String, Int)](("one", 2) -> ConfigValueFactory.fromAnyRef(Map("_1" -> "one", "_2" -> 2).asJava))
  checkRead[(String, Int)](("one", 2) -> ConfigValueFactory.fromIterable(List("one", 2).asJava))

  // Check writers
  checkWrite[Tuple1[Int]](new Tuple1(1) -> ConfigValueFactory.fromIterable(List(1).asJava))
  checkWrite[(String, Int)](("one", 2) -> ConfigValueFactory.fromIterable(List("one", 2).asJava))
  checkWrite[(Int, (Long, String), Boolean)]((1, (2l, "three"), false) -> ConfigValueFactory.fromIterable(List(1, List(2l, "three").asJava, false).asJava))

  // Check errors
  checkFailure[(String, Int), CannotConvert](ConfigValueFactory.fromAnyRef(Map("_1" -> "one", "_2" -> "two").asJava))
  checkFailure[(String, Int), CannotConvert](ConfigValueFactory.fromIterable(List("one", "two").asJava))
  checkFailures[(Int, Int, Int)](
    ConfigValueFactory.fromIterable(List(1, "one").asJava) -> ConfigReaderFailures(List(WrongSizeList(3, 2, None, ""))))
  checkFailures[(Int, Int, Int)](
    ConfigValueFactory.fromAnyRef(Map("_1" -> "one", "_2" -> 2).asJava) -> ConfigReaderFailures(List(
      CannotConvert("one", "Int", """java.lang.NumberFormatException: For input string: "one"""", None, "_1"),
      KeyNotFound("_3", None, Set()))))
}
