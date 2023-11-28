package pureconfig
package generic
package derivation

object syntax:
  export reader.syntax.*
  export writer.syntax.*
  export convert.syntax.*

@deprecated("Use pureconfig.generic.derivation.syntax instead", "0.18.0")
object default:
  export reader.syntax.derived
