package pureconfig

import shapeless.test.illTyped

class DerivationModesSuite2_12 extends BaseSuite {

  behavior of "semiauto"

  // this should apply to all Scala versions, but it seems that `illTyped` has different behavior on
  // earlier Scala versions that makes it fail with a compiler "unused import" lint.
  it should "not provide instance derivation for products and coproducts out-of-the-box" in {
    illTyped("{ import pureconfig.generic.semiauto._; loadConfig[Entity](conf) }")
    illTyped("{ import pureconfig.generic.semiauto._; ConfigWriter[Entity] }")
  }
}
