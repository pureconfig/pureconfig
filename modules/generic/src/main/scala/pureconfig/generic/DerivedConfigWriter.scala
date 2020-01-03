package pureconfig.generic

import com.typesafe.config._
import pureconfig._
import shapeless._

/**
 * A `ConfigWriter` derived with `shapeless`.
 *
 * @tparam A the type of objects writable by this `ConfigWriter`
 */
private[generic] trait DerivedConfigWriter[A] extends ConfigWriter[A]

object DerivedConfigWriter extends DerivedConfigWriter1 {

  implicit def anyValWriter[T, U](
    implicit
    ev: T <:< AnyVal,
    generic: Generic[T],
    unwrapped: Unwrapped.Aux[T, U],
    writer: ConfigWriter[U]): DerivedConfigWriter[T] =
    new DerivedConfigWriter[T] {
      override def to(t: T): ConfigValue = writer.to(unwrapped.unwrap(t))
    }

  implicit def tupleWriter[F: IsTuple, Repr](
    implicit
    gen: Generic.Aux[F, Repr],
    cc: SeqShapedWriter[Repr]): DerivedConfigWriter[F] = new DerivedConfigWriter[F] {
    override def to(t: F): ConfigValue =
      cc.to(gen.to(t))
  }
}

trait DerivedConfigWriter1 {

  // used for both products and coproducts
  final implicit def labelledGenericWriter[F, Repr](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[MapShapedWriter[F, Repr]]): DerivedConfigWriter[F] = new DerivedConfigWriter[F] {

    override def to(t: F): ConfigValue = {
      cc.value.to(gen.to(t))
    }
  }
}
