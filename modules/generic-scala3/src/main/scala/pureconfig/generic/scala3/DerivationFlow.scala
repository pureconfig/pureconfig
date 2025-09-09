package pureconfig.generic.scala3

/** Context of macro derivation.
  *
  * Instance of this class is intended to be passed as an inline paramter, with compiler knowing precise values of all
  * fields
  *
  * @param auto
  *   if true, full auto derivation is allowed
  * @param allowAutoSum
  *   if true, auto derivation if enums and sealed traits is currently allowed, either because auto = true or we are
  *   deriving an instance for a top-level union, that was requested in the derivation macro
  */
class DerivationFlow(val allowAutoSum: Boolean)

object DerivationFlow {
  inline def default = DerivationFlow(allowAutoSum = true)
  extension (inline df: DerivationFlow) {
    inline def throughProduct: DerivationFlow = DerivationFlow(allowAutoSum = false)
  }
}
