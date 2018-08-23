package pureconfig.module

import _root_.cats.data.{ NonEmptyList, NonEmptyMap, NonEmptySet, NonEmptyVector }
import _root_.cats.kernel.Order
import pureconfig.{ ConfigReader, ConfigWriter }

import scala.collection.immutable.{ SortedMap, SortedSet }
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
}
