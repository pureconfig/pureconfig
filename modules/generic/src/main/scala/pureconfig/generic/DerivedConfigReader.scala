package pureconfig.generic

import scala.annotation.unused

import shapeless._

import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.error.InvalidCoproductOption

/** A `ConfigReader` derived with `shapeless`.
  *
  * @tparam A
  *   the type of objects readable by this `ConfigReader`
  */
trait DerivedConfigReader[A] extends ConfigReader[A]

object DerivedConfigReader extends DerivedConfigReader1 {

  implicit def anyValReader[A, Wrapped](implicit
      ev: A <:< AnyVal,
      unwrapped: Unwrapped.Aux[A, Wrapped],
      reader: ConfigReader[Wrapped]
  ): DerivedConfigReader[A] =
    new DerivedConfigReader[A] {

      def from(value: ConfigCursor): ConfigReader.Result[A] =
        reader.from(value).map(unwrapped.wrap)
    }

  implicit def tupleReader[A, Repr <: HList, LabelledRepr <: HList, DefaultRepr <: HList](implicit
      @unused("Needed to disambiguate from anyValReader") isTuple: IsTuple[A],
      g: Generic.Aux[A, Repr],
      gcr: SeqShapedReader[Repr],
      lg: LabelledGeneric.Aux[A, LabelledRepr],
      default: Default.AsOptions.Aux[A, DefaultRepr],
      pr: MapShapedReader[A, LabelledRepr, DefaultRepr]
  ): DerivedConfigReader[A] =
    new DerivedConfigReader[A] {

      def from(cur: ConfigCursor) = {
        // Try to read first as the list representation and afterwards as the product representation (i.e. ConfigObject
        // with '_1', '_2', etc. keys).
        val cc = cur.asListCursor
          .map(Right.apply)
          .left
          .flatMap(failure => cur.asObjectCursor.map(Left.apply).left.map(_ => failure))

        cc.flatMap {
          case Right(listCur) => tupleAsListReader(listCur)
          case Left(objCur) => tupleAsObjectReader(objCur)
        }
      }
    }

  private[pureconfig] def tupleAsListReader[A, Repr <: HList](
      cur: ConfigListCursor
  )(implicit gen: Generic.Aux[A, Repr], cr: SeqShapedReader[Repr]): ConfigReader.Result[A] =
    cr.from(cur).map(gen.from)

  private[pureconfig] def tupleAsObjectReader[A, Repr <: HList, DefaultRepr <: HList](
      cur: ConfigObjectCursor
  )(implicit
      gen: LabelledGeneric.Aux[A, Repr],
      default: Default.AsOptions.Aux[A, DefaultRepr],
      cr: MapShapedReader[A, Repr, DefaultRepr]
  ): ConfigReader.Result[A] =
    cr.from(cur, default(), Set.empty).map(gen.from)
}

trait DerivedConfigReader1 {

  final implicit def productReader[A, Repr <: HList, DefaultRepr <: HList](implicit
      gen: LabelledGeneric.Aux[A, Repr],
      default: Default.AsOptions.Aux[A, DefaultRepr],
      cc: Lazy[MapShapedReader[A, Repr, DefaultRepr]]
  ): DerivedConfigReader[A] =
    new DerivedConfigReader[A] {

      override def from(cur: ConfigCursor): ConfigReader.Result[A] = {
        cur.asObjectCursor.flatMap(cc.value.from(_, default(), Set.empty)).map(gen.from)
      }
    }

  final implicit def coproductReader[A, Repr <: Coproduct](implicit
      gen: LabelledGeneric.Aux[A, Repr],
      hint: CoproductHint[A],
      readerOptions: CoproductReaderOptions[Repr]
  ): DerivedConfigReader[A] =
    new DerivedConfigReader[A] {

      override def from(cur: ConfigCursor): ConfigReader.Result[A] = {
        def readerFor(option: String) =
          readerOptions.options.get(option).map(_.map(gen.from))

        hint.from(cur, readerOptions.options.keys.toList.sorted).flatMap {
          case CoproductHint.Use(cursor, option) =>
            readerFor(option) match {
              case Some(value) => value.from(cursor)
              case None => ConfigReader.Result.fail[A](cursor.failureFor(InvalidCoproductOption(option)))
            }

          case CoproductHint.Attempt(cursor, options, combineF) =>
            val initial: Either[Vector[(String, ConfigReaderFailures)], A] = Left(Vector.empty)
            val res = options.foldLeft(initial) { (curr, option) =>
              curr.left.flatMap { currentFailures =>
                readerFor(option) match {
                  case Some(value) => value.from(cursor).left.map(f => currentFailures :+ (option -> f))
                  case None =>
                    Left(
                      currentFailures :+
                        (option -> ConfigReaderFailures(cursor.failureFor(InvalidCoproductOption(option))))
                    )
                }
              }
            }
            res.left.map(combineF)
        }
      }
    }
}
