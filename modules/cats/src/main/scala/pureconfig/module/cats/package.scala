package pureconfig.module

import scala.language.higherKinds
import scala.reflect.ClassTag

import _root_.cats.data.{ NonEmptyList, NonEmptyVector }
import com.typesafe.config.ConfigValue
import pureconfig.ConfigReader.{ fromFunction => fromFunctionReader }
import pureconfig.ConfigWriter.{ fromFunction => fromFunctionWriter }
import pureconfig.error.{ ConfigReaderFailures, ConfigValueLocation }
import pureconfig.{ ConfigReader, ConfigWriter }

/**
 * [[ConfigReader]] and [[ConfigWriter]] instances for cats data structures.
 */
package object cats {

  private[pureconfig] def fromNonEmpty[F[_], G[_], T](fromFT: F[T] => Option[G[T]])(configValue: ConfigValue)(implicit ct: ClassTag[F[T]], fReader: ConfigReader[F[T]]): Either[ConfigReaderFailures, G[T]] =
    fReader.from(configValue).right.flatMap { ft =>
      fromFT(ft) match {
        case None => Left(ConfigReaderFailures(EmptyTraversableFound(ct.toString, ConfigValueLocation(configValue), None)))
        case Some(nonEmpty) => Right(nonEmpty)
      }
    }

  implicit def nonEmptyListReader[T](implicit listReader: ConfigReader[List[T]]): ConfigReader[NonEmptyList[T]] =
    fromFunctionReader(fromNonEmpty[List, NonEmptyList, T](NonEmptyList.fromList))

  implicit def nonEmptyListWriter[T](implicit listWriter: ConfigWriter[List[T]]): ConfigWriter[NonEmptyList[T]] =
    fromFunctionWriter(nel => listWriter.to(nel.toList))

  implicit def nonEmptyVectorReader[T](implicit vectorReader: ConfigReader[Vector[T]]): ConfigReader[NonEmptyVector[T]] =
    fromFunctionReader(fromNonEmpty[Vector, NonEmptyVector, T](NonEmptyVector.fromVector))

  implicit def nonEmptyVectorWriter[T](implicit vectorWriter: ConfigWriter[Vector[T]]): ConfigWriter[NonEmptyVector[T]] =
    fromFunctionWriter(nonEmptyVector => vectorWriter.to(nonEmptyVector.toVector))
}
