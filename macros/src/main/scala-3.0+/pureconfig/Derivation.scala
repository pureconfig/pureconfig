package pureconfig

import scala.annotation.implicitNotFound

@implicitNotFound(
  """Cannot find an implicit instance of ${A}.
If you are trying to read or write a case class or sealed trait consider using PureConfig's auto derivation by adding `import pureconfig.generic.auto._`"""
)
sealed trait Derivation[A] {
  def value: A
}

object Derivation {

  // A derivation for which an implicit value of `A` could be found.
  case class Successful[A](value: A) extends Derivation[A]

  // A derivation for which an implicit `A` could be found. This is only used internally by the `materializeDerivation`
  // macro - when a derivation requested directly by a user is successfully materialized, it is guaranteed to be a
  // `Derivation.Successful`.
  case class Failed[A]() extends Derivation[A] {
    def value = throw new IllegalStateException("Illegal Derivation")
  }

  implicit def materializeDerivation[A](implicit value: A): Derivation[A] = Derivation.Successful(value)
}
