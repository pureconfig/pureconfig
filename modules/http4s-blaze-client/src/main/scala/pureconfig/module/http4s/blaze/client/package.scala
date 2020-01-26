package pureconfig.module.http4s.blaze

import cats.data.NonEmptyChain
import cats.effect.ConcurrentEffect
import cats.implicits._
import javax.net.ssl.SSLContext
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.{ConfigCursor, ConfigReader, ConfigWriter}

import scala.concurrent.ExecutionContext

package object client {

  implicit val uriReader: ConfigReader[Uri] =
    ConfigReader.fromString(
      str =>
        Uri
          .fromString(str)
          .fold(
            err => Left(CannotConvert(str, "Uri", err.sanitized)),
            uri => Right(uri)
        )
    )

  implicit val uriWriter: ConfigWriter[Uri] =
    ConfigWriter[String].contramap(_.renderString)

  implicit def blazeClientBuilderReader[F[_]: ConcurrentEffect]: ConfigReader[
    (ExecutionContext, Option[SSLContext]) => BlazeClientBuilder[F]
  ] = {
    def withField(map: (String, ConfigCursor)) = map match {
      case ("bufferSize", crs) =>
        crs.asInt.map { bufferSize =>
          Some((b: BlazeClientBuilder[F]) => b.withBufferSize(bufferSize))
        }
      case _ => Right(None)
    }
    ConfigReader
      .fromCursor[(ExecutionContext, Option[SSLContext]) => BlazeClientBuilder[
        F
      ]] { cur =>
        for {
          objCur <- cur.asObjectCursor
          transformations <- objCur.map.toList
            .map(withField)
            .map {
              _.leftMap(crfs => NonEmptyChain(crfs.head, crfs.tail: _*))
            }
            .sequence
            .leftMap(nec => ConfigReaderFailures(nec.head, nec.tail.toList))
            .map(_.flattenOption)
          constructor = { (ec: ExecutionContext, ssl: Option[SSLContext]) =>
            transformations.foldl(BlazeClientBuilder[F](ec, ssl)) {
              case (b, f) => f(b)
            }
          }
        } yield constructor
      }
  }

}
