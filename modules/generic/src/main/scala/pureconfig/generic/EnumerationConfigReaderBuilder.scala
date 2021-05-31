package pureconfig.generic

import shapeless._
import shapeless.labelled._

import pureconfig.ConfigReader.Result
import pureconfig.generic.error.NoValidCoproductOptionFound
import pureconfig.{ConfigCursor, ConfigReader}

/** A type class to build `ConfigReader`s for sealed families of case objects where each type is encoded as a
  * `ConfigString` based on the type name.
  *
  * @tparam A
  *   the type of objects capable of being read as an enumeration
  */
trait EnumerationConfigReaderBuilder[A] {
  def build(transformName: String => String): ConfigReader[A]
}

object EnumerationConfigReaderBuilder {
  implicit val deriveEnumerationReaderBuilderCNil: EnumerationConfigReaderBuilder[CNil] =
    new EnumerationConfigReaderBuilder[CNil] {
      def build(transformName: String => String): ConfigReader[CNil] =
        new ConfigReader[CNil] {
          def from(cur: ConfigCursor): Result[CNil] =
            cur.asConfigValue.flatMap(v => cur.failed(NoValidCoproductOptionFound(v, Seq.empty)))
        }
    }

  implicit def deriveEnumerationReaderBuilderCCons[K <: Symbol, H, T <: Coproduct](implicit
      vName: Witness.Aux[K],
      hGen: LabelledGeneric.Aux[H, HNil],
      tReaderBuilder: EnumerationConfigReaderBuilder[T]
  ): EnumerationConfigReaderBuilder[FieldType[K, H] :+: T] =
    new EnumerationConfigReaderBuilder[FieldType[K, H] :+: T] {
      def build(transformName: String => String): ConfigReader[FieldType[K, H] :+: T] = {
        lazy val tReader = tReaderBuilder.build(transformName)
        new ConfigReader[FieldType[K, H] :+: T] {
          def from(cur: ConfigCursor): Result[FieldType[K, H] :+: T] =
            cur.asString match {
              case Right(s) if s == transformName(vName.value.name) => Right(Inl(field[K](hGen.from(HNil))))
              case Right(_) => tReader.from(cur).map(Inr.apply)
              case Left(err) => Left(err)
            }
        }
      }
    }

  implicit def deriveEnumerationReaderBuilder[A, Repr <: Coproduct](implicit
      gen: LabelledGeneric.Aux[A, Repr],
      reprReaderBuilder: EnumerationConfigReaderBuilder[Repr]
  ): EnumerationConfigReaderBuilder[A] =
    new EnumerationConfigReaderBuilder[A] {
      def build(transformName: String => String): ConfigReader[A] = {
        reprReaderBuilder.build(transformName).map(gen.from)
      }
    }
}
