package pureconfig.generic.scala3

/** Context of macro derivation.
  *
  * An instance of this class is intended to be passed as an inline parameter, with the compiler knowing the precise
  * values of all fields
  *
  * @param allowAutoSum
  *   If true, auto derivation of enums and sealed traits is currently allowed, because we are deriving an instance for
  *   a top-level union
  */
class DerivationFlow(val allowAutoSum: Boolean)

object DerivationFlow {
  inline def default = DerivationFlow(allowAutoSum = true)
  extension (inline df: DerivationFlow) {
    inline def throughProduct: DerivationFlow = DerivationFlow(allowAutoSum = false)
  }
}
