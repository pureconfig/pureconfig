package pureconfig

import argonaut._, Argonaut._

case class Config(wrapper: WrapperExample)
case class WrapperExample(string: Wrappers.StringWrapper, int: Wrappers.IntWrapper, bool: Wrappers.BoolWrapper)

object Wrappers {
  case class StringWrapper(s: String)
  case class IntWrapper(i: Int)
  case class BoolWrapper(b: Boolean)

  implicit val stringWrapperEncode: EncodeJson[StringWrapper] = EncodeJson.of[String].contramap(_.s)
  implicit val intWrapperEncode: EncodeJson[IntWrapper] = EncodeJson.of[Int].contramap(_.i)
  implicit val boolWrapperEncode: EncodeJson[BoolWrapper] = EncodeJson.of[Boolean].contramap(_.b)
  implicit val stringWrapperDecode: DecodeJson[StringWrapper] = DecodeJson.of[String].map(StringWrapper)
  implicit val intWrapperDecode: DecodeJson[IntWrapper] = DecodeJson.of[Int].map(IntWrapper)
  implicit val boolWrapperDecode: DecodeJson[BoolWrapper] = DecodeJson.of[Boolean].map(BoolWrapper)
}
