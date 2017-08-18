package pureconfig.module

import scala.language.higherKinds
import scala.reflect.ClassTag

import _root_.cats.data.{ NonEmptyList, NonEmptyVector }
import pureconfig.error.{ ConfigReaderFailures, ConfigValueLocation }
import pureconfig.{ ConfigCursor, ConfigReader, ConfigWriter }

/**
 * `ConfigReader` and `ConfigWriter` instances for cats data structures.
 */
package object cats {

  private[pureconfig] def fromNonEmpty[F[_], G[_], T](fromFT: F[T] => Option[G[T]])(configValue: ConfigCursor)(implicit ct: ClassTag[F[T]], fReader: ConfigReader[F[T]]): Either[ConfigReaderFailures, G[T]] =
    fReader.from(configValue).right.flatMap { ft =>
      fromFT(ft) match {
        case None => Left(ConfigReaderFailures(EmptyTraversableFound(ct.toString, ConfigValueLocation(configValue.value), "")))
        case Some(nonEmpty) => Right(nonEmpty)
      }
    }

  implicit def nonEmptyListReader[T](implicit listReader: ConfigReader[List[T]]): ConfigReader[NonEmptyList[T]] =
    ConfigReader.fromCursor(fromNonEmpty[List, NonEmptyList, T](NonEmptyList.fromList))

  implicit def nonEmptyListWriter[T](implicit listWriter: ConfigWriter[List[T]]): ConfigWriter[NonEmptyList[T]] =
    ConfigWriter.fromFunction(nel => listWriter.to(nel.toList))

  implicit def nonEmptyVectorReader[T](implicit vectorReader: ConfigReader[Vector[T]]): ConfigReader[NonEmptyVector[T]] =
    ConfigReader.fromCursor(fromNonEmpty[Vector, NonEmptyVector, T](NonEmptyVector.fromVector))

  implicit def nonEmptyVectorWriter[T](implicit vectorWriter: ConfigWriter[Vector[T]]): ConfigWriter[NonEmptyVector[T]] =
    ConfigWriter.fromFunction(nonEmptyVector => vectorWriter.to(nonEmptyVector.toVector))
}
