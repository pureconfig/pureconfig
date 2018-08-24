package pureconfig.generic

import pureconfig._
import pureconfig.error._
import shapeless._

trait DerivedConfigReader[A] extends ConfigReader[A]

object DerivedConfigReader extends DerivedConfigReader1 {

  implicit def anyValReader[T, U](
    implicit
    ev: T <:< AnyVal,
    generic: Generic[T],
    unwrapped: Unwrapped.Aux[T, U],
    reader: ConfigReader[U]): DerivedConfigReader[T] = new DerivedConfigReader[T] {

    def from(value: ConfigCursor): Either[ConfigReaderFailures, T] =
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
      // Try to read first as the product representation (i.e.
      // ConfigObject with '_1', '_2', etc. keys) and afterwards as the Generic
      // representation (i.e. ConfigList).
      cur.asCollectionCursor.right.flatMap {
        case Right(objCur) => tupleAsObjectReader(objCur)
        case Left(_) => tupleAsListReader(cur)
      }
    }
  }

  private[pureconfig] def tupleAsListReader[F: IsTuple, Repr <: HList](cur: ConfigCursor)(
    implicit
    gen: Generic.Aux[F, Repr],
    cr: SeqShapedReader[Repr]): Either[ConfigReaderFailures, F] =
    cr.from(cur).right.map(gen.from)

  private[pureconfig] def tupleAsObjectReader[F: IsTuple, Repr <: HList, DefaultRepr <: HList](cur: ConfigObjectCursor)(
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cr: MapShapedReader.WithDefaults[F, Repr, DefaultRepr]): Either[ConfigReaderFailures, F] =
    cr.fromWithDefault(cur, default()).right.map(gen.from)
}

trait DerivedConfigReader1 {

  final implicit def productReader[F, Repr <: HList, DefaultRepr <: HList](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    default: Default.AsOptions.Aux[F, DefaultRepr],
    cc: Lazy[MapShapedReader.WithDefaults[F, Repr, DefaultRepr]]): DerivedConfigReader[F] = new DerivedConfigReader[F] {

    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, F] = {
      cur.asObjectCursor.right.flatMap(cc.value.fromWithDefault(_, default())).right.map(gen.from)
    }
  }

  final implicit def coproductReader[F, Repr <: Coproduct](
    implicit
    gen: LabelledGeneric.Aux[F, Repr],
    cc: Lazy[MapShapedReader[F, Repr]]): DerivedConfigReader[F] = new DerivedConfigReader[F] {

    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, F] = {
      cc.value.from(cur).right.map(gen.from)
    }
  }
}
