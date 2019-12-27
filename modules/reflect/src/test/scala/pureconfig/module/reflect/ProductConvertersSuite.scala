package pureconfig.module.reflect

import pureconfig.{BaseSuite, ConfigConvert, ConfigReader}
import org.scalacheck.ScalacheckShapeless._
import pureconfig.ConfigConvert.catchReadError

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



  // tests
  {
    //Make the implicit scope local
    implicit val flatConfigReader = ReflectConfigReaders.configReader7(FlatConfig)
    implicit val flatConfigWriter = ReflectConfigWriters.configWriter7((FlatConfig.unapply _).andThen(_.get))
    checkArbitrary[FlatConfig]
  }

  it should s"be able to override all of the ConfigConvert instances used to parse ${classOf[FlatConfig]}" in forAll {
    (config: FlatConfig) =>
      implicit val readBoolean = ConfigReader.fromString[Boolean](catchReadError(_ => false))
      implicit val readDouble = ConfigReader.fromString[Double](catchReadError(_ => 1D))
      implicit val readFloat = ConfigReader.fromString[Float](catchReadError(_ => 2F))
      implicit val readInt = ConfigReader.fromString[Int](catchReadError(_ => 3))
      implicit val readLong = ConfigReader.fromString[Long](catchReadError(_ => 4L))
      implicit val readString = ConfigReader.fromString[String](catchReadError(_ => "foobar"))
      implicit val readOption = ConfigConvert.viaString[Option[String]](catchReadError(_ => None), _ => " ")

      implicit val flatConfigReader = ReflectConfigReaders.configReader7(FlatConfig)
      implicit val flatConfigWriter = ReflectConfigWriters.configWriter7((FlatConfig.unapply _).andThen(_.get))
      val cc = ConfigConvert[FlatConfig]
      cc.from(cc.to(config)) shouldBe Right(FlatConfig(false, 1D, 2F, 3, 4L, "foobar", None))
  }

}
