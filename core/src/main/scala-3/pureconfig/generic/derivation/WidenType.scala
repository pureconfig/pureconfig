package pureconfig.generic.derivation

object WidenType {
  inline def widen[A, B](a: A): A & B =
    inline a match {
      case b: B => b
    }
}
