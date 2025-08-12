package pureconfig.generic.scala3

class DerivationFlow(val fallthroughUnions: Boolean) {
  inline def noFallthoughUnions: DerivationFlow = DerivationFlow(fallthroughUnions = false)
}

object DerivationFlow {
  inline def default = DerivationFlow(fallthroughUnions = true)
}
