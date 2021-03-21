package pureconfig.generic

import com.typesafe.config.ConfigValue
import pureconfig.ConfigWriter
import shapeless._
import shapeless.labelled._

/** A `ConfigWriter` for generic representations of coproducts.
  *
  * @tparam Original the original type for which `Repr` is the coproduct representation
  * @tparam Repr the generic representation
  */
private[generic] trait CoproductConfigWriter[Original, Repr <: Coproduct] extends ConfigWriter[Repr]

object CoproductConfigWriter {
  final implicit def cNilWriter[Original]: CoproductConfigWriter[Original, CNil] =
    new CoproductConfigWriter[Original, CNil] {
      override def to(t: CNil): ConfigValue =
        throw new IllegalStateException("Cannot encode CNil. This is likely a bug in PureConfig.")
    }

  final implicit def cConsWriter[Original, Name <: Symbol, V <: Original, T <: Coproduct](implicit
      coproductHint: CoproductHint[Original],
      vName: Witness.Aux[Name],
      vConfigWriter: Lazy[ConfigWriter[V]],
      tConfigWriter: Lazy[CoproductConfigWriter[Original, T]]
  ): CoproductConfigWriter[Original, FieldType[Name, V] :+: T] =
    new CoproductConfigWriter[Original, FieldType[Name, V] :+: T] {
      override def to(t: FieldType[Name, V] :+: T): ConfigValue =
        t match {
          case Inl(l) =>
            coproductHint.to(vConfigWriter.value.to(l), vName.value.name)

          case Inr(r) =>
            tConfigWriter.value.to(r)
        }
    }
}
