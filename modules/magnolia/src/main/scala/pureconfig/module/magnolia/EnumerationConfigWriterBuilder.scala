package pureconfig.module.magnolia

import scala.language.experimental.macros

import com.typesafe.config.{ConfigValue, ConfigValueFactory}
import magnolia._

import pureconfig.ConfigWriter

/** A type class to build `ConfigWriter`s for sealed families of case objects where each type is encoded as a
  * `ConfigString` based on the type name.
  *
  * @tparam A the type of objects capable of being written as an enumeration
  */
private[magnolia] trait EnumerationConfigWriterBuilder[A] {
  def build(transformName: String => String): ConfigWriter[A]
}

object EnumerationConfigWriterBuilder {
  type Typeclass[A] = EnumerationConfigWriterBuilder[A]

  def combine[A](ctx: CaseClass[EnumerationConfigWriterBuilder, A]): EnumerationConfigWriterBuilder[A] =
    new EnumerationConfigWriterBuilder[A] {
      def build(transformName: String => String): ConfigWriter[A] =
        new ConfigWriter[A] {
          def to(a: A): ConfigValue = ConfigValueFactory.fromAnyRef(transformName(ctx.typeName.short))
        }
    }

  def dispatch[A](ctx: SealedTrait[EnumerationConfigWriterBuilder, A]): EnumerationConfigWriterBuilder[A] =
    new EnumerationConfigWriterBuilder[A] {
      def build(transformName: String => String): ConfigWriter[A] =
        new ConfigWriter[A] {
          def to(a: A): ConfigValue =
            ctx.dispatch(a)(subtype => subtype.typeclass.build(transformName).to(subtype.cast(a)))
        }
    }

  implicit def export[A]: EnumerationConfigWriterBuilder[A] = macro Magnolia.gen[A]
}
