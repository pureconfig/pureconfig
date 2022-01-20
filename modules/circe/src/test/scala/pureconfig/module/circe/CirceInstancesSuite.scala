package pureconfig.module.circe

import io.circe._
import io.circe.literal._

import pureconfig._

class CirceInstancesSuite extends BaseSuite {

  import pureconfig.module.circe.instances._

  "Secret Encoder" should "encode a Secret instance to JSON masking the value" in {
    Encoder[Secret[Int]].apply(Secret(1)).noSpaces shouldBe "\"** MASKED **\""
  }

  "Secret Decoder" should "decode a Json Int into a Secret instance" in {
    Decoder[Secret[Int]].decodeJson(json"1") shouldEqual Right(Secret(1))
  }
}
