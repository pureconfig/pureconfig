package pureconfig.module.catseffect

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.effect.{Blocker, ContextShift, Sync}

import pureconfig.module.catseffect
import pureconfig.{ConfigReader, ConfigSource, Derivation}

package object syntax {

  implicit class CatsEffectConfigSource(private val cs: ConfigSource) extends AnyVal {

    @inline
    final def loadF[F[_], A](
        blocker: Blocker
    )(implicit F: Sync[F], csf: ContextShift[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
      catseffect.loadF(cs, blocker)

    @deprecated("Use `cs.loadF[F, A](blocker)` instead", "0.12.3")
    def loadF[F[_], A](implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
      catseffect.loadF(cs)
  }
}
