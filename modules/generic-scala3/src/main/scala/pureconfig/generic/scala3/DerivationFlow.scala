package pureconfig.generic.scala3

/** Context of macro derivation
  *
  * @param auto
  *   if true, full auto derivation is allowed
  * @param allowAutoUnion
  *   if true, auto derivation if enums and sealed traits is currently allowed, either because auto = true or we are
  *   deriving an instance for a top-level union, that was requested in the derivation macro
  */
class DerivationFlow(val auto: Boolean, val allowAutoUnion: Boolean)

object DerivationFlow {
  inline def auto = DerivationFlow(auto = true, allowAutoUnion = true)
  inline def semiauto = DerivationFlow(auto = false, allowAutoUnion = true)
  extension (inline df: DerivationFlow) {
    inline def throughProduct: DerivationFlow =
      DerivationFlow(auto = df.auto, allowAutoUnion = df.auto)
  }
}
