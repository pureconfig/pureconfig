package pureconfig.module

import _root_.scalaz.{==>>, ICons, IList, INil, ISet, Maybe, NonEmptyList, Order}

import pureconfig._
import pureconfig.error.FailureReason

/** `ConfigReader` and `ConfigWriter` instances for `scalaz` data structures.
  */
package object scalaz {

  case object EmptyIListFound extends FailureReason {
    def description: String =
      "Empty scalaz.IList found when trying to convert to scalaz.NonEmptyList."
  }

  implicit def iListConvert[A](implicit cc: ConfigConvert[List[A]]): ConfigConvert[IList[A]] =
    cc.xmap(IList.fromList, _.toList)

  implicit def iSetConvert[A: Order](implicit cc: ConfigConvert[List[A]]): ConfigConvert[ISet[A]] =
    cc.xmap(l => ISet.fromList(l), _.toList)

  implicit def mapConvert[A: Order, B](implicit cc: ConfigConvert[Map[A, B]]): ConfigConvert[A ==>> B] =
    cc.xmap(m => ==>>.fromList(m.toList), _.toList.toMap)

  implicit def nonEmptyListReader[A](implicit cr: ConfigReader[IList[A]]): ConfigReader[NonEmptyList[A]] =
    cr.emap {
      case ICons(h, t) => Right(NonEmptyList.nel(h, t))
      case INil() => Left(EmptyIListFound)
    }

  implicit def nonEmptyListWriter[A](implicit cw: ConfigWriter[IList[A]]): ConfigWriter[NonEmptyList[A]] =
    cw.contramap(_.list)

  implicit def maybeConvert[A](implicit convert: ConfigConvert[Option[A]]): ConfigConvert[Maybe[A]] =
    convert.xmap(Maybe.fromOption, _.toOption)
}
