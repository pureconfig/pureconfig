package pureconfig.generic

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import pureconfig.ConfigWriter
import shapeless._
import shapeless.labelled._

trait EnumerationConfigWriterBuilder[A] {
  def build(transformName: String => String): ConfigWriter[A]
}

object EnumerationConfigWriterBuilder {
  implicit val deriveEnumerationWriterBuilderCNil: EnumerationConfigWriterBuilder[CNil] =
    new EnumerationConfigWriterBuilder[CNil] {
      def build(transformName: String => String): ConfigWriter[CNil] =
        new ConfigWriter[CNil] {
          def to(a: CNil): ConfigValue =
            throw new IllegalStateException("Cannot encode CNil. This is likely a bug in PureConfig.")
        }
    }

  implicit def deriveEnumerationWriterBuilderCCons[K <: Symbol, H, T <: Coproduct](
    implicit
    vName: Witness.Aux[K],
    hGen: LabelledGeneric.Aux[H, HNil],
    tWriterBuilder: EnumerationConfigWriterBuilder[T]): EnumerationConfigWriterBuilder[FieldType[K, H] :+: T] =
    new EnumerationConfigWriterBuilder[FieldType[K, H] :+: T] {
      def build(transformName: String => String): ConfigWriter[FieldType[K, H] :+: T] = {
        lazy val tWriter = tWriterBuilder.build(transformName)
        new ConfigWriter[FieldType[K, H] :+: T] {
          def to(a: FieldType[K, H] :+: T): ConfigValue = a match {
            case Inl(_) => ConfigValueFactory.fromAnyRef(transformName(vName.value.name))
            case Inr(r) => tWriter.to(r)
          }
        }
      }
    }

  implicit def deriveEnumerationWriterBuilder[A, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[A, Repr],
    reprWriterBuilder: EnumerationConfigWriterBuilder[Repr]): EnumerationConfigWriterBuilder[A] =
    new EnumerationConfigWriterBuilder[A] {
      def build(transformName: String => String): ConfigWriter[A] = {
        lazy val reprWriter = reprWriterBuilder.build(transformName)
        new ConfigWriter[A] {
          def to(a: A): ConfigValue = reprWriter.to(gen.to(a))
        }
      }
    }
}
