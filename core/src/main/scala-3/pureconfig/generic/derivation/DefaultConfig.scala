package pureconfig
package generic
package derivation

trait DefaultDerivationConfig:
  given [A]: ProductHint[A] = ProductHint.default[A]
  given [A]: CoproductHint[A] = CoproductHint.default[A]

trait DefaultEnumDerivationConfig:
  given [A]: EnumHint[A] = EnumHint.default[A]

object defaults extends DefaultDerivationConfig, DefaultEnumDerivationConfig
