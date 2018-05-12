package pureconfig

import shapeless.test.illTyped

final class IntWrapper(val inner: Int) extends AnyVal {
  override def toString: String = s"IntWrapper($inner)"
}

final class PrivateFloatValue private (val value: Float) extends AnyVal

final class GenericValue[T] private (val t: T) extends AnyVal {
  override def toString: String = "GenericValue(" + t.toString + ")"
}

object GenericValue {
  def from[T](t: T): GenericValue[T] = new GenericValue[T](t)
}

trait Read[T] {
  def read(str: String): T
}

object Read {
  def apply[T](implicit readT: Read[T]): Read[T] = readT

  implicit val badReadString = new Read[String] {
    override def read(str: String): String = ""
  }
}

class ValueClassSuite extends BaseSuite {

  behavior of "ConfigConvert for Value Classes"

  checkRead[IntWrapper](ConfigWriter.forPrimitive[Int].to(1) -> new IntWrapper(1))
  checkWrite[IntWrapper](new IntWrapper(1) -> ConfigWriter.forPrimitive[Int].to(1))

  "ConfigReader[PrivateFloatValue]" should "not be derivable because the constructor is private" in {
    illTyped("pureconfig.ConfigReader[PrivateFloatValue]")
  }

  {
    implicit def genericValueReader[T](implicit readT: Read[T]): ConfigReader[GenericValue[T]] =
      ConfigReader.fromString(s => Right(GenericValue.from(readT.read(s))))

    "ConfigReader" should " should be able to override value classes ConfigReader" in {
      val reader = ConfigReader[GenericValue[String]]
      val expected = Right(GenericValue.from(""))
      val configValue = ConfigWriter.forPrimitive[String].to("foobar")
      reader.from(configValue) shouldEqual expected
    }
  }
}
