package pureconfig

import scala.language.higherKinds

import com.typesafe.config.ConfigFactory

import pureconfig.module.magnolia.auto.reader._
import pureconfig.module.magnolia.auto.writer._

class CoproductConvertersSuite2_12 extends BaseSuite {

  behavior of "ConfigConvert"

  it should "read disambiguation information on sealed families with the cases nested in the companion" in {
    import CarMaker._
    val conf = ConfigFactory.parseString("{ type = bmw }")
    ConfigConvert[CarMaker].from(conf.root()) shouldEqual Right(BMW)
  }
}

sealed trait CarMaker

object CarMaker {
  case object Mercedes extends CarMaker
  case object BMW extends CarMaker
  case object Tesla extends CarMaker
}
