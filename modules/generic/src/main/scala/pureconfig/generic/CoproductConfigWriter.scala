package pureconfig.generic

import com.typesafe.config.ConfigValue
import pureconfig.{ ConfigWriter, Derivation }
import shapeless.labelled._
import shapeless._

/**
 * A `ConfigWriter` for generic representations of coproducts.
 *
 * @tparam Wrapped the original type for which `Repr` is the coproduct representation
 * @tparam Repr the generic representation
 */
private[generic] trait CoproductConfigWriter[Wrapped, Repr <: Coproduct] extends ConfigWriter[Repr]

object CoproductConfigWriter {
  final implicit def cNilWriter[Wrapped]: CoproductConfigWriter[Wrapped, CNil] = new CoproductConfigWriter[Wrapped, CNil] {
    override def to(t: CNil): ConfigValue =
      throw new IllegalStateException("Cannot encode CNil. This is likely a bug in PureConfig.")
  }

  final implicit def cConsWriter[Wrapped, Name <: Symbol, V <: Wrapped, T <: Coproduct](
    implicit
    coproductHint: CoproductHint[Wrapped],
    vName: Witness.Aux[Name],
    vConfigWriter: Derivation[Lazy[ConfigWriter[V]]],
    tConfigWriter: Lazy[CoproductConfigWriter[Wrapped, T]]): CoproductConfigWriter[Wrapped, FieldType[Name, V] :+: T] =
    new CoproductConfigWriter[Wrapped, FieldType[Name, V] :+: T] {
      override def to(t: FieldType[Name, V] :+: T): ConfigValue = t match {
        case Inl(l) =>
          coproductHint.to(vConfigWriter.value.value.to(l), vName.value.name)

        case Inr(r) =>
          tConfigWriter.value.to(r)
      }
    }
}
