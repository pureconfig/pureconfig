package pureconfig.generic

import pureconfig._
import shapeless._

/**
 * A `ConfigReader` derived with `shapeless`.
 *
 * @tparam A the type of objects readable by this `ConfigReader`
 */
trait DerivedConfigReader[A] extends ConfigReader[A]

object DerivedConfigReader extends DerivedConfigReader1 {

  implicit def anyValReader[A, Wrapped](
    implicit
    ev: A <:< AnyVal,
    generic: Generic[A],
    unwrapped: Unwrapped.Aux[A, Wrapped],
    reader: ConfigReader[Wrapped]): DerivedConfigReader[A] = new DerivedConfigReader[A] {

    def from(value: ConfigCursor): ConfigReader.Result[A] =
      reader.from(value).right.map(unwrapped.wrap)
  }

  implicit def tupleReader[A: IsTuple, Repr <: HList, LabelledRepr <: HList, DefaultRepr <: HList](
    implicit
    g: Generic.Aux[A, Repr],
    gcr: SeqShapedReader[Repr],
    lg: LabelledGeneric.Aux[A, LabelledRepr],
    default: Default.AsOptions.Aux[A, DefaultRepr],
    pr: MapShapedReader.WithDefaults[A, LabelledRepr, DefaultRepr]): DerivedConfigReader[A] = new DerivedConfigReader[A] {

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

  private[pureconfig] def tupleAsListReader[A: IsTuple, Repr <: HList](cur: ConfigListCursor)(
    implicit
    gen: Generic.Aux[A, Repr],
    cr: SeqShapedReader[Repr]): ConfigReader.Result[A] =
    cr.from(cur).right.map(gen.from)

  private[pureconfig] def tupleAsObjectReader[A: IsTuple, Repr <: HList, DefaultRepr <: HList](cur: ConfigObjectCursor)(
    implicit
    gen: LabelledGeneric.Aux[A, Repr],
    default: Default.AsOptions.Aux[A, DefaultRepr],
    cr: MapShapedReader.WithDefaults[A, Repr, DefaultRepr]): ConfigReader.Result[A] =
    cr.fromWithDefault(cur, default()).right.map(gen.from)
}

trait DerivedConfigReader1 {

  final implicit def productReader[A, Repr <: HList, DefaultRepr <: HList](
    implicit
    gen: LabelledGeneric.Aux[A, Repr],
    default: Default.AsOptions.Aux[A, DefaultRepr],
    cc: Lazy[MapShapedReader.WithDefaults[A, Repr, DefaultRepr]]): DerivedConfigReader[A] = new DerivedConfigReader[A] {

    override def from(cur: ConfigCursor): ConfigReader.Result[A] = {
      cur.asObjectCursor.right.flatMap(cc.value.fromWithDefault(_, default())).right.map(gen.from)
    }
  }

  final implicit def coproductReader[A, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[A, Repr],
    cc: Lazy[MapShapedReader[A, Repr]]): DerivedConfigReader[A] = new DerivedConfigReader[A] {

    override def from(cur: ConfigCursor): ConfigReader.Result[A] = {
      cc.value.from(cur).right.map(gen.from)
    }
  }
}
