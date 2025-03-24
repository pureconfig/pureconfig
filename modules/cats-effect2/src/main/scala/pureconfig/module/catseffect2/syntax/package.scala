package pureconfig.module.catseffect2

import scala.reflect.ClassTag

import cats.effect.{Blocker, ContextShift, Sync}

import pureconfig.module.catseffect2
import pureconfig.{ConfigReader, ConfigSource}

package object syntax {

  implicit class CatsEffectConfigSource(private val cs: ConfigSource) extends AnyVal {

    @inline
    final def loadF[F[_]: Sync: ContextShift, A: ConfigReader](blocker: Blocker)(implicit ct: ClassTag[A]): F[A] =
      catseffect2.loadF(cs, blocker)
  }
}
