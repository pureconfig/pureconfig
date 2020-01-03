package pureconfig.generic

import pureconfig._
import shapeless._

/**
 * A `ConfigReader` derived with `shapeless`.
 *
 * @tparam A the type of objects readable by this `ConfigReader`
 */
private[generic] trait DerivedConfigReader[A] extends ConfigReader[A]

object DerivedConfigReader extends DerivedConfigReader1 {

  implicit def anyValReader[T, U](
    implicit
    ev: T <:< AnyVal,
    generic: Generic[T],
    unwrapped: Unwrapped.Aux[T, U],
    reader: ConfigReader[U]): DerivedConfigReader[T] = new DerivedConfigReader[T] {

    def from(value: ConfigCursor): ConfigReader.Result[T] =
      reader.from(value).right.map(unwrapped.wrap)
  }

  implicit def tupleReader[F: IsTuple, Repr <: HList, LRepr <: HList, DefaultRepr <: HList](
    implicit
    g: Generic.Aux[F, Repr],
    gcr: SeqShapedReader[Repr],
    lg: LabelledGeneric.Aux[F, LRepr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    pr: MapShapedReader.WithDefaults[F, LRepr, DefaultRepr]): DerivedConfigReader[F] = new DerivedConfigReader[F] {

    def from(cur: ConfigCursor) = {
      // Try to read first as the list representation and afterwards as the product representation (i.e. ConfigObject
      // with '_1', '_2', etc. keys).
      val cc = cur.asListCursor.right.map(Right.apply).left.flatMap(failure =>
        cur.asObjectCursor.right.map(Left.apply).left.map(_ => failure))

      cc.right.flatMap {
        case Right(listCur) => tupleAsListReader(listCur)
        case Left(objCur) => tupleAsObjectReader(objCur)
      }
    }
  }

  private[pureconfig] def tupleAsListReader[F: IsTuple, Repr <: HList](cur: ConfigListCursor)(
    implicit
    gen: Generic.Aux[F, Repr],
    cr: SeqShapedReader[Repr]): ConfigReader.Result[F] =
    cr.from(cur).right.map(gen.from)

  private[pureconfig] def tupleAsObjectReader[F: IsTuple, Repr <: HList, DefaultRepr <: HList](cur: ConfigObjectCursor)(
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cr: MapShapedReader.WithDefaults[F, Repr, DefaultRepr]): ConfigReader.Result[F] =
    cr.fromWithDefault(cur, default(), Set.empty).right.map(gen.from)
}

trait DerivedConfigReader1 {

  final implicit def productReader[F, Repr <: HList, DefaultRepr <: HList](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cc: Lazy[MapShapedReader.WithDefaults[F, Repr, DefaultRepr]]): DerivedConfigReader[F] = new DerivedConfigReader[F] {

    override def from(cur: ConfigCursor): ConfigReader.Result[F] = {
      cur.asObjectCursor.right.flatMap(cc.value.fromWithDefault(_, default(), Set.empty)).right.map(gen.from)
    }
  }

  final implicit def coproductReader[F, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[MapShapedReader[F, Repr]]): DerivedConfigReader[F] = new DerivedConfigReader[F] {

    override def from(cur: ConfigCursor): ConfigReader.Result[F] = {
      cc.value.from(cur, Nil).right.map(gen.from)
    }
  }
}
