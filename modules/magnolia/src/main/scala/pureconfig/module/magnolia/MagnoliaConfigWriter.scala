package pureconfig.module.magnolia

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import _root_.magnolia._
import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import pureconfig._
import pureconfig.error.ConfigReaderException
import pureconfig.generic.{ CoproductHint, ProductHint }

/**
 * An object containing Magnolia `combine` and `dispatch` methods to generate `ConfigWriter` instances.
 */
object MagnoliaConfigWriter {

  def combine[A](ctx: CaseClass[ConfigWriter, A])(implicit hint: ProductHint[A]): ConfigWriter[A] =
    if (ctx.typeName.full.startsWith("scala.Tuple")) combineTuple(ctx)
    else if (ctx.isValueClass) combineValueClass(ctx)
    else combineCaseClass(ctx)

  private def combineCaseClass[A](ctx: CaseClass[ConfigWriter, A])(implicit hint: ProductHint[A]): ConfigWriter[A] = new ConfigWriter[A] {
    def to(a: A): ConfigValue = {
      val fieldValues = ctx.parameters.map { param =>
        val valueOpt = param.typeclass match {
          case w: WritesMissingKeys[param.PType @unchecked] =>
            w.toOpt(param.dereference(a))
          case w =>
            Some(w.to(param.dereference(a)))
        }
        hint.to(param.label, valueOpt)
      }
      ConfigValueFactory.fromMap(fieldValues.flatten.toMap.asJava)
    }
  }

  private def combineTuple[A](ctx: CaseClass[ConfigWriter, A]): ConfigWriter[A] = new ConfigWriter[A] {
    override def to(a: A): ConfigValue = ConfigValueFactory.fromIterable(
      ctx.parameters.map { param => param.typeclass.to(param.dereference(a)) }.asJava)
  }

  private def combineValueClass[A](ctx: CaseClass[ConfigWriter, A]): ConfigWriter[A] = new ConfigWriter[A] {
    override def to(a: A): ConfigValue =
      ctx.parameters.map { param => param.typeclass.to(param.dereference(a)) }.head
  }

  def dispatch[A: ClassTag](ctx: SealedTrait[ConfigWriter, A])(implicit hint: CoproductHint[A]): ConfigWriter[A] = new ConfigWriter[A] {
    def to(a: A): ConfigValue = ctx.dispatch(a) { subtype =>
      hint.to(subtype.typeclass, subtype.typeName.short, subtype.cast(a))
        .fold(fs => throw new ConfigReaderException[A](fs), identity)
    }
  }
}
