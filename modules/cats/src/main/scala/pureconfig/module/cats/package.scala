package pureconfig.module

import _root_.cats.data._
import _root_.cats.kernel.Order
import _root_.cats.{ Alternative, Foldable }
import _root_.cats.implicits._
import pureconfig.{ ConfigReader, ConfigWriter, Exported }

import scala.collection.immutable.{ SortedMap, SortedSet }
import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * `ConfigReader` and `ConfigWriter` instances for cats data structures.
 */
package object cats {

  private[pureconfig] def fromNonEmpty[X, Y](reader: ConfigReader[X])(fromX: X => Option[Y])(implicit ct: ClassTag[X]): ConfigReader[Y] =
    reader.emap(x => fromX(x).toRight(EmptyTraversableFound(ct.toString)))

  implicit def nonEmptyListReader[T](implicit reader: ConfigReader[List[T]]): ConfigReader[NonEmptyList[T]] =
    fromNonEmpty(reader)(NonEmptyList.fromList)
  implicit def nonEmptyListWriter[T](implicit writer: ConfigWriter[List[T]]): ConfigWriter[NonEmptyList[T]] =
    writer.contramap(_.toList)

  implicit def nonEmptyVectorReader[T](implicit reader: ConfigReader[Vector[T]]): ConfigReader[NonEmptyVector[T]] =
    fromNonEmpty(reader)(NonEmptyVector.fromVector)
  implicit def nonEmptyVectorWriter[T](implicit writer: ConfigWriter[Vector[T]]): ConfigWriter[NonEmptyVector[T]] =
    writer.contramap(_.toVector)

  implicit def nonEmptySetReader[T](implicit reader: ConfigReader[SortedSet[T]]): ConfigReader[NonEmptySet[T]] =
    fromNonEmpty(reader)(NonEmptySet.fromSet)
  implicit def nonEmptySetWriter[T](implicit writer: ConfigWriter[SortedSet[T]]): ConfigWriter[NonEmptySet[T]] =
    writer.contramap(_.toSortedSet)

  implicit def nonEmptyMapReader[A, B](implicit reader: ConfigReader[Map[A, B]], ord: Order[A]): ConfigReader[NonEmptyMap[A, B]] =
    fromNonEmpty(reader)(x => NonEmptyMap.fromMap(SortedMap(x.toSeq: _*)(ord.toOrdering)))
  implicit def nonEmptyMapWriter[A, B](implicit writer: ConfigWriter[Map[A, B]]): ConfigWriter[NonEmptyMap[A, B]] =
    writer.contramap(_.toSortedMap)

  // For emptiable foldables not covered by TraversableOnce reader/writer, e.g. Chain.
  implicit def lowPriorityNonReducibleReader[T, F[_]: Foldable: Alternative](implicit reader: ConfigReader[TraversableOnce[T]]): Exported[ConfigReader[F[T]]] =
    Exported(reader.map(to => (to foldRight Alternative[F].empty[T])(_.pure[F] <+> _)))
  implicit def lowPriorityNonReducibleWriter[T, F[_]: Foldable: Alternative](implicit writer: ConfigWriter[TraversableOnce[T]]): Exported[ConfigWriter[F[T]]] =
    Exported(writer.contramap(_.toList))

  implicit def nonEmptyChainReader[T](implicit reader: ConfigReader[Chain[T]]): ConfigReader[NonEmptyChain[T]] =
    fromNonEmpty(reader)(NonEmptyChain.fromChain)
  implicit def nonEmptyChainWriter[T](implicit writer: ConfigWriter[Chain[T]]): ConfigWriter[NonEmptyChain[T]] =
    writer.contramap(_.toChain)
}
