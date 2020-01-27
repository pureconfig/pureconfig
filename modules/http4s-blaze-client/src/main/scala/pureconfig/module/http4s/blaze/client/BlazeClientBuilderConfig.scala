package pureconfig.module.http4s.blaze.client

import cats.effect.ConcurrentEffect
import javax.net.ssl.SSLContext
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait BlazeClientBuilderConfig {
  def configure[F[_]: ConcurrentEffect](
    ec: ExecutionContext,
    ssl: Option[SSLContext] = None): BlazeClientBuilder[F]
}
