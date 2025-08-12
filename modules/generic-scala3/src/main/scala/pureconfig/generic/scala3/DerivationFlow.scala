package pureconfig.generic.scala3

class DerivationFlow(val auto: Boolean, val allowAutoUnion: Boolean)

object DerivationFlow {
  inline def auto = DerivationFlow(auto = true, allowAutoUnion = true)
  inline def semiauto = DerivationFlow(auto = false, allowAutoUnion = true)
  extension (inline df: DerivationFlow) {
    inline def throughProduct: DerivationFlow =
      DerivationFlow(auto = df.auto, allowAutoUnion = df.auto)
  }
}
