package pureconfig.module

import _root_.cats.data._
import _root_.cats.kernel.Order
import _root_.cats.{Alternative, Foldable}
import _root_.cats.implicits._
import pureconfig.{ConfigReader, ConfigWriter, Exported}

import scala.collection.immutable.{SortedMap, SortedSet}
import scala.language.higherKinds
import scala.reflect.ClassTag

/**
  * `ConfigReader` and `ConfigWriter` instances for cats data structures.
  */
package object cats {

  private[pureconfig] def fromNonEmpty[A, B](
      reader: ConfigReader[A]
  )(fromX: A => Option[B])(implicit ct: ClassTag[A]): ConfigReader[B] =
    reader.emap(x => fromX(x).toRight(EmptyTraversableFound(ct.toString)))

  implicit def nonEmptyListReader[A](implicit reader: ConfigReader[List[A]]): ConfigReader[NonEmptyList[A]] =
    fromNonEmpty(reader)(NonEmptyList.fromList)
  implicit def nonEmptyListWriter[A](implicit writer: ConfigWriter[List[A]]): ConfigWriter[NonEmptyList[A]] =
    writer.contramap(_.toList)

  implicit def nonEmptyVectorReader[A](implicit reader: ConfigReader[Vector[A]]): ConfigReader[NonEmptyVector[A]] =
    fromNonEmpty(reader)(NonEmptyVector.fromVector)
  implicit def nonEmptyVectorWriter[A](implicit writer: ConfigWriter[Vector[A]]): ConfigWriter[NonEmptyVector[A]] =
    writer.contramap(_.toVector)

  implicit def nonEmptySetReader[A](implicit reader: ConfigReader[SortedSet[A]]): ConfigReader[NonEmptySet[A]] =
    fromNonEmpty(reader)(NonEmptySet.fromSet)
  implicit def nonEmptySetWriter[A](implicit writer: ConfigWriter[SortedSet[A]]): ConfigWriter[NonEmptySet[A]] =
    writer.contramap(_.toSortedSet)

  implicit def nonEmptyMapReader[A, B](implicit
      reader: ConfigReader[Map[A, B]],
      ord: Order[A]
  ): ConfigReader[NonEmptyMap[A, B]] =
    fromNonEmpty(reader)(x => NonEmptyMap.fromMap(SortedMap(x.toSeq: _*)(ord.toOrdering)))
  implicit def nonEmptyMapWriter[A, B](implicit writer: ConfigWriter[Map[A, B]]): ConfigWriter[NonEmptyMap[A, B]] =
    writer.contramap(_.toSortedMap)

  // For emptiable foldables not covered by TraversableOnce reader/writer, e.g. Chain.
  implicit def lowPriorityNonReducibleReader[A, F[_]: Foldable: Alternative](implicit
      reader: ConfigReader[List[A]]
  ): Exported[ConfigReader[F[A]]] =
    Exported(reader.map(to => (to foldRight Alternative[F].empty[A])(_.pure[F] <+> _)))
  implicit def lowPriorityNonReducibleWriter[A, F[_]: Foldable: Alternative](implicit
      writer: ConfigWriter[List[A]]
  ): Exported[ConfigWriter[F[A]]] =
    Exported(writer.contramap(_.toList))

  implicit def nonEmptyChainReader[A](implicit reader: ConfigReader[Chain[A]]): ConfigReader[NonEmptyChain[A]] =
    fromNonEmpty(reader)(NonEmptyChain.fromChain)
  implicit def nonEmptyChainWriter[A](implicit writer: ConfigWriter[Chain[A]]): ConfigWriter[NonEmptyChain[A]] =
    writer.contramap(_.toChain)
}
