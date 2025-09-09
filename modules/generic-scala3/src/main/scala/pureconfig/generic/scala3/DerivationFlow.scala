package pureconfig.generic.scala3

/** Context of macro derivation.
  *
  * Instance of this class is intended to be passed as an inline paramter, with compiler knowing precise values of all
  * fields
  *
  * @param allowAutoSum
  *   if true, auto derivation of enums and sealed traits is currently allowed, becausewe are deriving an instance for a
  *   top-level union
  */
class DerivationFlow(val allowAutoSum: Boolean)

object DerivationFlow {
  inline def default = DerivationFlow(allowAutoSum = true)
  extension (inline df: DerivationFlow) {
    inline def throughProduct: DerivationFlow = DerivationFlow(allowAutoSum = false)
  }
}
