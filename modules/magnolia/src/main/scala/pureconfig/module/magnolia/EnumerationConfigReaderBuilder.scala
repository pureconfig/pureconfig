package pureconfig.module.magnolia

import scala.language.experimental.macros
import scala.reflect.ClassTag

import magnolia1._

import pureconfig.ConfigReader.Result
import pureconfig.error.CannotConvert
import pureconfig.generic.error.NoValidCoproductOptionFound
import pureconfig.{ConfigCursor, ConfigReader}

/** A type class to build `ConfigReader`s for sealed families of case objects where each type is encoded as a
  * `ConfigString` based on the type name.
  *
  * @tparam A
  *   the type of objects capable of being read as an enumeration
  */
private[magnolia] trait EnumerationConfigReaderBuilder[A] {
  def build(transformName: String => String): ConfigReader[A]
}

object EnumerationConfigReaderBuilder {
  type Typeclass[A] = EnumerationConfigReaderBuilder[A]

  def join[A](
      ctx: CaseClass[EnumerationConfigReaderBuilder, A]
  )(implicit ct: ClassTag[A]): EnumerationConfigReaderBuilder[A] =
    new EnumerationConfigReaderBuilder[A] {
      def build(transformName: String => String): ConfigReader[A] =
        new ConfigReader[A] {
          def from(cur: ConfigCursor): Result[A] =
            if (ctx.isObject) Right(ctx.rawConstruct(Seq.empty))
            else
              cur.asString.flatMap(v =>
                cur.failed(
                  CannotConvert(
                    v,
                    ct.runtimeClass.getName,
                    s"An enumeration reader was derived for a sealed family in which subtype ${ct.runtimeClass.getName} isn't an object"
                  )
                )
              )
        }
    }

  def split[A](ctx: SealedTrait[EnumerationConfigReaderBuilder, A]): EnumerationConfigReaderBuilder[A] =
    new EnumerationConfigReaderBuilder[A] {
      def build(transformName: String => String): ConfigReader[A] =
        new ConfigReader[A] {
          def from(cur: ConfigCursor): Result[A] = {
            cur.asString.flatMap { stringValue =>
              ctx.subtypes.find(typ => transformName(typ.typeName.short) == stringValue) match {
                case Some(typ) => typ.typeclass.build(transformName).from(cur)
                case None => cur.asConfigValue.flatMap(v => cur.failed(NoValidCoproductOptionFound(v, Seq.empty)))
              }
            }
          }
        }
    }

  implicit def export[A]: EnumerationConfigReaderBuilder[A] = macro Magnolia.gen[A]
}
