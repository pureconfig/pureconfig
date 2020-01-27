package pureconfig.module.http4s.blaze.server

import org.http4s.server.blaze.BlazeServerBuilder

import scala.language.higherKinds

private[server] trait BlazeServerBuilderTransformer {
  def apply[F[_]]: BlazeServerBuilder[F] => BlazeServerBuilder[F]
}
