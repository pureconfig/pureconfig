package pureconfig

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

sealed trait Derivation[A] {
  def value: A
}

object Derivation {

  // A derivation for which an implicit value of `A` could be found.
  case class Successful[A](value: A) extends Derivation[A]

  implicit def materializeDerivation[A]: Derivation[A] = macro DerivationMacros.materializeDerivation[A]
}

@macrocompat.bundle
class DerivationMacros(val c: whitebox.Context) {
  import c.universe._

  // Derivations are not supported on Scala 2.10. This is a shim executing a regular implicit search.
  def materializeDerivation[A: WeakTypeTag]: Tree = {
    q"_root_.pureconfig.Derivation.Successful(implicitly[${weakTypeOf[A]}])"
  }
}
