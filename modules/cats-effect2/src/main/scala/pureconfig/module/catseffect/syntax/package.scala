package pureconfig.module.catseffect

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.effect.{Blocker, ContextShift, Sync}

import pureconfig.module.catseffect
import pureconfig.{ConfigReader, ConfigSource}

package object syntax {

  implicit class CatsEffectConfigSource(private val cs: ConfigSource) extends AnyVal {

    @inline
    final def loadF[F[_]: Sync: ContextShift, A: ConfigReader](blocker: Blocker)(implicit ct: ClassTag[A]): F[A] =
      catseffect.loadF(cs, blocker)
  }
}
