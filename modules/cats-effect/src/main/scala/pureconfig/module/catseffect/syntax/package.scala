package pureconfig.module.catseffect

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.effect.{ Blocker, ContextShift, Sync }
import pureconfig.ConfigSource
import pureconfig.module.catseffect

package object syntax {

  implicit class CatsEffectConfigSource(private val cs: ConfigSource) extends AnyVal {
    @inline
    final def loadF[F[_]: Sync: ContextShift, A: ConfReader: ClassTag](blocker: Blocker): F[A] =
      catseffect.loadF(cs, blocker)
  }
}
