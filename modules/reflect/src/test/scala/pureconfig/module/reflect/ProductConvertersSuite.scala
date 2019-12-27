package pureconfig.module.reflect

import pureconfig.BaseSuite
import org.scalacheck.ScalacheckShapeless._

class ProductConvertersSuite
    extends BaseSuite {
  behavior of "ConfigConvert"

  /* A configuration with only simple values and `Option` */
  case class FlatConfig(b: Boolean,
                        d: Double,
                        f: Float,
                        i: Int,
                        l: Long,
                        s: String,
                        o: Option[String])

  implicit val flatConfigReader = ReflectConfigReaders.configReader7(FlatConfig)
  implicit val flatConfigWriter = ReflectConfigWriters.configWriter7((FlatConfig.unapply _).andThen(_.get))

  // tests
  checkArbitrary[FlatConfig]

}
