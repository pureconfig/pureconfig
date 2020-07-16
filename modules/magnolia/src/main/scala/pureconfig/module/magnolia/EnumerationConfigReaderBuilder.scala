package pureconfig.module.magnolia

import scala.language.experimental.macros

import magnolia._
import pureconfig.ConfigReader.Result
import pureconfig.generic.error.NoValidCoproductOptionFound
import pureconfig.{ConfigCursor, ConfigReader}

/**
  * A type class to build `ConfigReader`s for sealed families of case objects where each type is encoded as a
  * `ConfigString` based on the type name.
  *
  * @tparam A the type of objects capable of being read as an enumeration
  */
private[magnolia] trait EnumerationConfigReaderBuilder[A] {
  def build(transformName: String => String): ConfigReader[A]
}

object EnumerationConfigReaderBuilder {
  type Typeclass[A] = EnumerationConfigReaderBuilder[A]

  def combine[A](ctx: CaseClass[EnumerationConfigReaderBuilder, A]): EnumerationConfigReaderBuilder[A] =
    new EnumerationConfigReaderBuilder[A] {
      def build(transformName: String => String): ConfigReader[A] =
        new ConfigReader[A] {
          def from(cur: ConfigCursor): Result[A] =
            if (ctx.isObject) Right(ctx.rawConstruct(Seq.empty))
            else cur.asConfigValue.right.flatMap(v => cur.failed(NoValidCoproductOptionFound(v, Seq.empty)))
        }
    }

  def dispatch[A](ctx: SealedTrait[EnumerationConfigReaderBuilder, A]): EnumerationConfigReaderBuilder[A] =
    new EnumerationConfigReaderBuilder[A] {
      def build(transformName: String => String): ConfigReader[A] =
        new ConfigReader[A] {
          def from(cur: ConfigCursor): Result[A] = {
            cur.asString.flatMap { stringValue =>
              ctx.subtypes.find(typ => transformName(typ.typeName.short) == stringValue) match {
                case Some(typ) => typ.typeclass.build(transformName).from(cur)
                case None => cur.asConfigValue.right.flatMap(v => cur.failed(NoValidCoproductOptionFound(v, Seq.empty)))
              }
            }
          }
        }
    }

  implicit def export[A]: EnumerationConfigReaderBuilder[A] = macro Magnolia.gen[A]
}
