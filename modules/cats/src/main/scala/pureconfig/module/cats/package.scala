package pureconfig.module

import _root_.cats.data.{ NonEmptyList, NonEmptySet, NonEmptyVector }
import pureconfig.error.ConfigReaderFailures
import pureconfig.{ ConfigCursor, ConfigReader, ConfigWriter }

import scala.collection.immutable.SortedSet
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
    ConfigWriter.fromFunction(nel => writer.to(nel.toSortedSet))
}
