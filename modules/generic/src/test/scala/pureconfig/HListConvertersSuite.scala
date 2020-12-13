package pureconfig

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigValueFactory
import org.scalacheck.ScalacheckShapeless._
import pureconfig.error.{ConfigReaderFailures, ConvertFailure, WrongSizeList}
import pureconfig.generic.auto._
import pureconfig.generic.hlist._
import shapeless._

class HListConvertersSuite extends BaseSuite {

  behavior of "ConfigConvert"

  // Check arbitrary HLists
  checkArbitrary[Int :: HNil]
  checkArbitrary[String :: Int :: HNil]
  checkArbitrary[Int :: (Long :: String :: HNil) :: Boolean :: HNil]

  // Check arbitrary HList with custom types
  case class Foo(a: Int, b: String)
  checkArbitrary[Long :: Foo :: Boolean :: Foo :: HNil]

  // Check HNil
  val emptyConfigList = ConfigValueFactory.fromIterable(List().asJava)
  checkRead[HNil](emptyConfigList -> HNil)
  checkWrite[HNil](HNil -> emptyConfigList)

  // Check WrongSizeList failures
  checkFailures[Int :: Int :: String :: HNil](
    ConfigValueFactory.fromIterable(List(1, 2, "three", 4).asJava) -> ConfigReaderFailures(
      ConvertFailure(WrongSizeList(3, 4), emptyConfigOrigin, "")
    )
  )
}
