package pureconfig.module.catseffect

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.effect.Sync
import pureconfig.{ ConfigReader, ConfigSource, Derivation }
import pureconfig.module.catseffect

package object syntax {
  implicit class CatsEffectConfigSource(val cs: ConfigSource) extends AnyVal {
    def loadF[F[_], A](implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
      catseffect.loadF(cs)
  }
}
