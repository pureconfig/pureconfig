package pureconfig
package generic
package derivation

trait EnumHint[A]:
  def transformName(name: String): String

private[pureconfig] final case class EnumHintImpl[A](fieldMapping: ConfigFieldMapping) extends EnumHint[A]:
  def transformName(name: String): String = fieldMapping(name)

object EnumHint:
  def apply[A](fieldMapping: ConfigFieldMapping = ConfigFieldMapping(PascalCase, KebabCase)): EnumHint[A] =
    EnumHintImpl[A](fieldMapping)

  // TODO: should we keep this?

  /** For compatibility with `ProductHint` and `CoproductHint` only. It could be replaced with `given [A]: Conversion[A,
    * EnumHint[A]] = _ => default[A]` but it would require to import `EnumHint.given` since `implicit` is going to be
    * deprecated at some point, and we need to import givens explicitly anyway. It's recommended to use the: `import
    * pureconfig.generic.derivation.defaults.given` instead.
    */
  implicit def default[A]: EnumHint[A] = apply()
