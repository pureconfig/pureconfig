package pureconfig
package generic

import pureconfig.generic.semiauto._

// enum Foo {
//   case Bar(value: Int)
//   case Baz(value: String)
// }

// final class FooWrapper private (val value: Foo) extends AnyVal

final class IntWrapper(val inner: Int) extends AnyVal {
  override def toString: String = s"IntWrapper($inner)"
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

//   given ConfigConvert[IntWrapper] = deriveConvert
  // given ConfigReader[IntWrapper] = deriveReader
  // given ConfigWriter[IntWrapper] = deriveWriter

  // {
  //   given ConfigReader[FooWrapper] = deriveReader

  //   // checkRead[IntWrapper](ConfigWriter.forPrimitive[Int].to(1) -> new IntWrapper(1))
  // }

  {
    given ConfigReader[IntWrapper] = deriveReader

    checkRead[IntWrapper](ConfigWriter.forPrimitive[Int].to(1) -> new IntWrapper(1))
  }

  {
    given ConfigWriter[IntWrapper] = deriveWriter

    checkWrite[IntWrapper](new IntWrapper(1) -> ConfigWriter.forPrimitive[Int].to(1))
  }

//   "ConfigReader[PrivateFloatValue]" should "not be derivable because the constructor is private" in {
//     illTyped("pureconfig.ConfigReader[PrivateFloatValue]")
//   }

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
