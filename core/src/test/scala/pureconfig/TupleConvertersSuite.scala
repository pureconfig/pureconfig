package pureconfig

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigValueFactory
import org.scalacheck.Shapeless._

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
}
