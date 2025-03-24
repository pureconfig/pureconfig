package pureconfig.module

import scala.reflect.ClassTag

import _root_.enum.Enum

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.viaNonEmptyString
import pureconfig.error.CannotConvert

package object `enum` {
  implicit def enumConfigConvert[A](implicit e: Enum[A], ct: ClassTag[A]): ConfigConvert[A] = {
    viaNonEmptyString(
      s => e.decode(s).left.map(failure => CannotConvert(s, ct.runtimeClass.getSimpleName, failure.toString)),
      e.encode
    )
  }
}
