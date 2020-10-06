package pureconfig.generic

import com.typesafe.config._
import pureconfig._
import shapeless._

/** A `ConfigWriter` derived with `shapeless`.
  *
  * @tparam A the type of objects writable by this `ConfigWriter`
  */
trait DerivedConfigWriter[A] extends ConfigWriter[A]

object DerivedConfigWriter extends DerivedConfigWriter1 {

  implicit def anyValWriter[A, Wrapped](implicit
      ev: A <:< AnyVal,
      generic: Generic[A],
      unwrapped: Unwrapped.Aux[A, Wrapped],
      writer: ConfigWriter[Wrapped]
  ): DerivedConfigWriter[A] =
    new DerivedConfigWriter[A] {
      override def to(t: A): ConfigValue = writer.to(unwrapped.unwrap(t))
    }

  implicit def tupleWriter[A: IsTuple, Repr](implicit
      gen: Generic.Aux[A, Repr],
      cc: SeqShapedWriter[Repr]
  ): DerivedConfigWriter[A] =
    new DerivedConfigWriter[A] {
      override def to(t: A): ConfigValue =
        cc.to(gen.to(t))
    }
}

trait DerivedConfigWriter1 {

  final implicit def productWriter[A, Repr <: HList](implicit
      gen: LabelledGeneric.Aux[A, Repr],
      cc: Lazy[MapShapedWriter[A, Repr]]
  ): DerivedConfigWriter[A] =
    new DerivedConfigWriter[A] {

      override def to(t: A): ConfigValue = {
        cc.value.to(gen.to(t))
      }
    }

  final implicit def coproductWriter[F, Repr <: Coproduct](implicit
      gen: LabelledGeneric.Aux[F, Repr],
      cw: Lazy[CoproductConfigWriter[F, Repr]]
  ): DerivedConfigWriter[F] =
    new DerivedConfigWriter[F] {
      def to(t: F): ConfigValue = {
        cw.value.to(gen.to(t))
      }
    }
}
