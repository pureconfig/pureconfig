package pureconfig.module.http4s.blaze

import java.net.InetSocketAddress

import cats.effect.{ ConcurrentEffect, Timer }
import cats.implicits._
import org.http4s.server.{ KeyStoreBits, defaults }
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.error.ConfigReaderFailures
import pureconfig.{ ConfigCursor, ConfigReader }

import scala.concurrent.duration.Duration
import scala.language.higherKinds

package object server {

  implicit val configReaderKeyStoreBits: ConfigReader[KeyStoreBits] = {
    import pureconfig.generic.auto._
    pureconfig.generic.semiauto.deriveReader[KeyStoreBits]
  }

  implicit def configReaderBlazeServerBuilderConfig(
    implicit
    KS: ConfigReader[KeyStoreBits]): ConfigReader[BlazeServerBuilderConfig] =
    ConfigReader.fromCursor[BlazeServerBuilderConfig] { cur =>
      for {
        objCur <- cur.asObjectCursor
        fields = objCur.map
        transformations <- fields.toList
          .traverse(parseField(KS))
          .map(_.flattenOption)
        transformationSocket <- parseSocket( // needs special care, because there are no `withHost` or `withPort` methods, only `bindSocketAddress`
          fields.get("host"),
          fields.get("port"))
        transformationsAll = transformationSocket.toList ++ transformations
      } yield new BlazeServerBuilderConfig {
        def configure[F[_]: ConcurrentEffect: Timer](): BlazeServerBuilder[F] = {
          transformationsAll.foldl(BlazeServerBuilder[F]) {
            case (b, f) => f.apply(b)
          }
        }
      }
    }

  private def parseField(KS: ConfigReader[KeyStoreBits])(
    map: (String, ConfigCursor)): Either[ConfigReaderFailures, Option[BlazeServerBuilderTransformer]] =
    map match {
      case ("responseHeaderTimeout", crs) =>
        ConfigReader[Duration].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withResponseHeaderTimeout(value)
          })
        }
      case ("idleTimeout", crs) =>
        ConfigReader[Duration].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withIdleTimeout(value)
          })
        }
      case ("nio2", crs) =>
        ConfigReader[Boolean].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withNio2(value)
          })
        }
      case ("connectorPoolSize", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withConnectorPoolSize(value)
          })
        }
      case ("bufferSize", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withBufferSize(value)
          })
        }
      case ("webSockets", crs) =>
        ConfigReader[Boolean].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withWebSockets(value)
          })
        }
      case ("enableHttp2", crs) =>
        ConfigReader[Boolean].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.enableHttp2(value)
          })
        }
      case ("maxRequestLineLength", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withMaxRequestLineLength(value)
          })
        }
      case ("maxHeadersLength", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withMaxHeadersLength(value)
          })
        }
      case ("chunkBufferMaxSize", crs) =>
        ConfigReader[Int].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withChunkBufferMaxSize(value)
          })
        }
      case ("banner", crs) =>
        ConfigReader[List[String]].from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] = _.withBanner(value)
          })
        }
      case ("keyStoreBits", crs) =>
        KS.from(crs).map { value =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] =
              _.withSSL(
                value.keyStore,
                value.keyManagerPassword,
                value.protocol,
                value.trustStore,
                value.clientAuth)
          })
        }
      case _ => Right(None)
    }

  private def parseSocket(
    host: Option[ConfigCursor],
    port: Option[ConfigCursor]): Either[ConfigReaderFailures, Option[BlazeServerBuilderTransformer]] =
    (host.map(_.asString), port.map(_.asInt)) match {
      case (Some(host), Some(port)) =>
        (host, port).mapN {
          case (host, port) =>
            Some(new BlazeServerBuilderTransformer {
              override def apply[F[_]] =
                _.bindSocketAddress(
                  InetSocketAddress.createUnresolved(host, port))
            })
        }
      case (Some(host), None) =>
        host.map { host =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] =
              _.bindSocketAddress(
                InetSocketAddress
                  .createUnresolved(host, defaults.SocketAddress.getPort))
          })
        }
      case (None, Some(port)) =>
        port.map { port =>
          Some(new BlazeServerBuilderTransformer {
            override def apply[F[_]] =
              _.bindSocketAddress(
                InetSocketAddress.createUnresolved(
                  defaults.SocketAddress.getAddress.getHostAddress,
                  port))
          })
        }
      case _ => Right(None)
    }

}
