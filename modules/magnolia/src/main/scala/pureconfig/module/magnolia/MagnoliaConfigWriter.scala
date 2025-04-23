package pureconfig.module.magnolia

import scala.jdk.CollectionConverters._

import _root_.magnolia1._
import com.typesafe.config.{ConfigValue, ConfigValueFactory}

import pureconfig._
import pureconfig.generic.{CoproductHint, ProductHint}

/** An object containing Magnolia `join` and `split` methods to generate `ConfigWriter` instances.
  */
object MagnoliaConfigWriter {

  def join[A](ctx: CaseClass[ConfigWriter, A])(implicit hint: ProductHint[A]): ConfigWriter[A] =
    if (ctx.typeName.full.startsWith("scala.Tuple")) joinTuple(ctx)
    else if (ctx.isValueClass) joinValueClass(ctx)
    else joinCaseClass(ctx)

  private def joinCaseClass[A](ctx: CaseClass[ConfigWriter, A])(implicit hint: ProductHint[A]): ConfigWriter[A] =
    new ConfigWriter[A] {
      def to(a: A): ConfigValue = {
        val fieldValues = ctx.parameters.map { param =>
          val valueOpt = param.typeclass match {
            case tc: WritesMissingKeys[param.PType @unchecked] =>
              tc.toOpt(param.dereference(a))
            case tc =>
              Some(tc.to(param.dereference(a)))
          }
          hint.to(valueOpt, param.label)
        }
        ConfigValueFactory.fromMap(fieldValues.flatten.toMap.asJava)
      }
    }

  private def joinTuple[A](ctx: CaseClass[ConfigWriter, A]): ConfigWriter[A] =
    new ConfigWriter[A] {
      override def to(a: A): ConfigValue =
        ConfigValueFactory.fromIterable(ctx.parameters.map { param => param.typeclass.to(param.dereference(a)) }.asJava)
    }

  private def joinValueClass[A](ctx: CaseClass[ConfigWriter, A]): ConfigWriter[A] =
    new ConfigWriter[A] {
      override def to(a: A): ConfigValue =
        ctx.parameters.map { param => param.typeclass.to(param.dereference(a)) }.head
    }

  def split[A](ctx: SealedTrait[ConfigWriter, A])(implicit hint: CoproductHint[A]): ConfigWriter[A] =
    new ConfigWriter[A] {
      def to(a: A): ConfigValue =
        ctx.split(a) { subtype =>
          hint.to(subtype.typeclass.to(subtype.cast(a)), subtype.typeName.short)
        }
    }
}
