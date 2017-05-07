package pureconfig

final class IntWrapper(val inner: Int) extends AnyVal {
  override def toString: String = s"IntWrapper($inner)"
}

class ValueClassSuite extends BaseSuite {

  behavior of "ConfigConvert for Value Classes"

  checkRead[IntWrapper](new IntWrapper(1) -> ConfigWriter.forPrimitive[Int].to(1))
  checkWrite[IntWrapper](new IntWrapper(1) -> ConfigWriter.forPrimitive[Int].to(1))
}
