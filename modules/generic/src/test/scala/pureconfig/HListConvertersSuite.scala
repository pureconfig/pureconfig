package pureconfig

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigValueFactory
import org.scalacheck.{Arbitrary, Gen}
import shapeless._

import pureconfig.error.{ConfigReaderFailures, ConvertFailure, WrongSizeList}
import pureconfig.generic.auto._
import pureconfig.generic.hlist._

class HListConvertersSuite extends BaseSuite {
  case class Foo(a: Int, b: String)

  implicit val arbFoo: Arbitrary[Foo] = Arbitrary {
    Arbitrary.arbitrary[(Int, String)].map((Foo.apply _).tupled)
  }

  implicit val arbHNil: Arbitrary[HNil] = Arbitrary(Gen.const(HNil))

  implicit def arbHCons[H: Arbitrary, T <: HList: Arbitrary]: Arbitrary[H :: T] = Arbitrary {
    Arbitrary.arbitrary[(H, T)].map { case (h, t) => h :: t }
  }

  behavior of "ConfigConvert"

  // Check arbitrary HLists
  checkArbitrary[Int :: HNil]
  checkArbitrary[String :: Int :: HNil]
  checkArbitrary[Int :: (Long :: String :: HNil) :: Boolean :: HNil]

  // Check arbitrary HList with custom types
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
