package pureconfig

import shapeless.test.illTyped

final class IntWrapper(val inner: Int) extends AnyVal {
  override def toString: String = s"IntWrapper($inner)"
}

final class PrivateFloatValue private (val value: Float) extends AnyVal

class ValueClassSuite extends BaseSuite {

  behavior of "ConfigConvert for Value Classes"

  checkRead[IntWrapper](new IntWrapper(1) -> ConfigWriter.forPrimitive[Int].to(1))
  checkWrite[IntWrapper](new IntWrapper(1) -> ConfigWriter.forPrimitive[Int].to(1))

  "ConfigReader[PrivateFloatValue]" should "not be derivable because the constructor is private" in {
    illTyped("pureconfig.ConfigReader[PrivateFloatValue]")
  }
}
