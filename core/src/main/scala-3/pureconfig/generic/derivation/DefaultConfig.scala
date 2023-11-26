package pureconfig.generic.derivation

import pureconfig.generic.{CoproductHint, ProductHint}

trait DefaultDerivationConfig:
  given [A]: ProductHint[A] = ProductHint.default[A]
  given [A]: CoproductHint[A] = CoproductHint.default[A]
