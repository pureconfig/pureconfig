package pureconfig.module

import _root_.cats.data.{ NonEmptyList, NonEmptyMap, NonEmptySet, NonEmptyVector }
import _root_.cats.kernel.Order
import pureconfig.error.ConfigReaderFailures
import pureconfig.{ ConfigCursor, ConfigReader, ConfigWriter }

import scala.collection.immutable.{ SortedMap, SortedSet }
import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * `ConfigReader` and `ConfigWriter` instances for cats data structures.
 */
package object cats {

  private[pureconfig] def fromNonEmpty[F[_], G[_], T](fromFT: F[T] => Option[G[T]])(cur: ConfigCursor)(implicit ct: ClassTag[F[T]], fReader: ConfigReader[F[T]]): Either[ConfigReaderFailures, G[T]] =
    fReader.from(cur).right.flatMap { ft =>
      fromFT(ft) match {
        case None => cur.failed(EmptyTraversableFound(ct.toString))
        case Some(nonEmpty) => Right(nonEmpty)
      }
    }
  private[pureconfig] def fromNonEmpty2[F[_, _], G[_, _], A, B](fromFT: F[A, B] => Option[G[A, B]])(cur: ConfigCursor)(implicit ct: ClassTag[F[A, B]], fReader: ConfigReader[F[A, B]]): Either[ConfigReaderFailures, G[A, B]] =
    fReader.from(cur).right.flatMap { ft =>
      fromFT(ft) match {
        case None ⇒ cur.failed(EmptyTraversableFound(ct.toString))
        case Some(nonEmpty) ⇒ Right(nonEmpty)
      }
    }

  implicit def nonEmptyListReader[T](implicit reader: ConfigReader[List[T]]): ConfigReader[NonEmptyList[T]] =
    ConfigReader.fromCursor(fromNonEmpty[List, NonEmptyList, T](NonEmptyList.fromList))
  implicit def nonEmptyListWriter[T](implicit writer: ConfigWriter[List[T]]): ConfigWriter[NonEmptyList[T]] =
    ConfigWriter.fromFunction(nel => writer.to(nel.toList))

  implicit def nonEmptyVectorReader[T](implicit reader: ConfigReader[Vector[T]]): ConfigReader[NonEmptyVector[T]] =
    ConfigReader.fromCursor(fromNonEmpty[Vector, NonEmptyVector, T](NonEmptyVector.fromVector))
  implicit def nonEmptyVectorWriter[T](implicit writer: ConfigWriter[Vector[T]]): ConfigWriter[NonEmptyVector[T]] =
    ConfigWriter.fromFunction(nonEmptyVector => writer.to(nonEmptyVector.toVector))

  implicit def nonEmptySetReader[T](implicit reader: ConfigReader[SortedSet[T]]): ConfigReader[NonEmptySet[T]] =
    ConfigReader.fromCursor(fromNonEmpty[SortedSet, NonEmptySet, T](NonEmptySet.fromSet))
  implicit def nonEmptySetWriter[T](implicit writer: ConfigWriter[SortedSet[T]]): ConfigWriter[NonEmptySet[T]] =
    ConfigWriter.fromFunction(nonEmptySet => writer.to(nonEmptySet.toSortedSet))

  implicit def nonEmptyMapReader[A, B](implicit reader: ConfigReader[Map[A, B]], ord: Ordering[A]): ConfigReader[NonEmptyMap[A, B]] =
    ConfigReader.fromCursor(fromNonEmpty2[Map, NonEmptyMap, A, B](v ⇒ NonEmptyMap.fromMap(SortedMap(v.toArray: _*))(Order.fromOrdering[A])))
  implicit def nonEmptyMapWriter[A, B](implicit writer: ConfigWriter[Map[A, B]]): ConfigWriter[NonEmptyMap[A, B]] =
    ConfigWriter.fromFunction(nonEmptyMap ⇒ writer.to(nonEmptyMap.toSortedMap))
}
