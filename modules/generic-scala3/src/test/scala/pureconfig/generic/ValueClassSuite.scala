package pureconfig
package generic

import pureconfig.generic.semiauto._

final class IntWrapperA(val inner: Int) extends AnyVal {
  override def toString: String = s"IntWrapperA($inner)"
}

final class IntWrapperB(val inner: Int) extends AnyVal {
  override def toString: String = s"IntWrapperB($inner)"
}

final class IntWrapperC(val inner: Int) extends AnyVal {
  override def toString: String = s"IntWrapperC($inner)"
}

final class PrivateFloatValue private (val value: Float) extends AnyVal

final class GenericValue[A] private (val t: A) extends AnyVal {
  override def toString: String = "GenericValue(" + t.toString + ")"
}

object GenericValue {
  def from[A](t: A): GenericValue[A] = new GenericValue[A](t)
}

class ValueClassSuite extends BaseSuite {

  behavior of "ConfigConvert for Value Classes"

  {
    given ConfigReader[IntWrapperA] = deriveReader

    checkRead[IntWrapperA](ConfigWriter.forPrimitive[Int].to(1) -> IntWrapperA(1))
  }

  {
    given ConfigWriter[IntWrapperB] = deriveWriter

    checkWrite[IntWrapperB](IntWrapperB(1) -> ConfigWriter.forPrimitive[Int].to(1))
  }

  {
    given ConfigConvert[IntWrapperC] = deriveConvert

    checkReadWrite[IntWrapperC](configValue("1") -> IntWrapperC(1))
  }

  "ConfigReader[PrivateFloatValue]" should "not be derivable because the constructor is private" in {
    """
    given ConfigReader[PrivateFloatValue] = deriveReader
    """ shouldNot compile
  }

  {
    trait Read[A] {
      def read(str: String): A
    }

    given Read[String] = new Read[String] {
      override def read(str: String): String = ""
    }

    given [A: Read]: ConfigReader[GenericValue[A]] =
      ConfigReader.fromString(s => Right(GenericValue.from(summon[Read[A]].read(s))))

    "ConfigReader" should " should be able to override value classes ConfigReader" in {
      val reader = ConfigReader[GenericValue[String]]
      val expected = Right(GenericValue.from(""))
      val configValue = ConfigWriter.forPrimitive[String].to("foobar")
      reader.from(configValue) shouldEqual expected
    }
  }
}
