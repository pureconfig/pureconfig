package pureconfig.module.http4s.blaze

import cats.effect.ConcurrentEffect
import cats.implicits._
import javax.net.ssl.SSLContext
import org.http4s.client.blaze.{ BlazeClientBuilder, ParserMode }
import org.http4s.headers.`User-Agent`
import pureconfig.error.ConfigReaderFailures
import pureconfig.{ ConfigCursor, ConfigReader }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.language.higherKinds

package object client {

  implicit val configReaderParserMode: ConfigReader[ParserMode] = {
    import pureconfig.generic.auto._
    pureconfig.generic.semiauto.deriveReader[ParserMode]
  }

  implicit val configReaderUserAgent: ConfigReader[`User-Agent`] = {
    import pureconfig.generic.auto._
    pureconfig.generic.semiauto.deriveReader[`User-Agent`]
  }

  implicit def configReaderBlazeClientBuilderConfig(
    implicit
    PM: ConfigReader[ParserMode],
    UA: ConfigReader[`User-Agent`]): ConfigReader[BlazeClientBuilderConfig] =
    ConfigReader.fromCursor[BlazeClientBuilderConfig] { cur =>
      for {
        objCur <- cur.asObjectCursor
        transformations <- objCur.map.toList
          .traverse(parseField(PM, UA))
          .map(_.flattenOption)
      } yield new BlazeClientBuilderConfig {
        def configure[F[_]: ConcurrentEffect](
          ec: ExecutionContext,
          ssl: Option[SSLContext] = None): BlazeClientBuilder[F] = {
          transformations.foldl(BlazeClientBuilder[F](ec, ssl)) {
            case (b, f) => f.apply(b)
          }
        }
      }
    }

  private def parseField(
    PM: ConfigReader[ParserMode],
    UA: ConfigReader[`User-Agent`])(
    map: (String, ConfigCursor)): Either[ConfigReaderFailures, Option[BlazeClientBuilderTransformer]] =
    map match {
      case ("responseHeaderTimeout", crs) =>
        ConfigReader[Duration].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withResponseHeaderTimeout(value)
          })
        }
      case ("idleTimeout", crs) =>
        ConfigReader[Duration].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withIdleTimeout(value)
          })
        }
      case ("requestTimeout", crs) =>
        ConfigReader[Duration].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withRequestTimeout(value)
          })
        }
      case ("maxTotalConnections", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withMaxTotalConnections(value)
          })
        }
      case ("maxWaitQueueLimit", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withMaxWaitQueueLimit(value)
          })
        }
      case ("checkEndpointAuthentication", crs) =>
        ConfigReader[Boolean].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withCheckEndpointAuthentication(value)
          })
        }
      case ("maxResponseLineSize", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withMaxResponseLineSize(value)
          })
        }
      case ("maxHeaderLength", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withMaxHeaderLength(value)
          })
        }
      case ("maxChunkSize", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withMaxChunkSize(value)
          })
        }
      case ("chunkBufferMaxSize", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withChunkBufferMaxSize(value)
          })
        }
      case ("parserMode", crs) =>
        PM.from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withParserMode(value)
          })
        }
      case ("bufferSize", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withBufferSize(value)
          })
        }
      case ("userAgent", crs) =>
        UA.from(crs).map { value =>
          Some(new BlazeClientBuilderTransformer {
            override def apply[F[_]] = _.withUserAgent(value)
          })
        }
      case _ => Right(None)
    }

}
