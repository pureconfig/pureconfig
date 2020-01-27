package pureconfig.module.http4s.blaze.server

import cats.effect.{ ConcurrentEffect, Timer }
import org.http4s.server.blaze.BlazeServerBuilder

import scala.language.higherKinds

trait BlazeServerBuilderConfig {
  def configure[F[_]: ConcurrentEffect: Timer](): BlazeServerBuilder[F]
}
