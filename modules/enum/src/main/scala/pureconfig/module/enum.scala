package pureconfig.module

import _root_.enum.Enum
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.fromNonEmptyStringConvert
import pureconfig.error.CannotConvert

import scala.reflect.ClassTag

package object enum {
  implicit def enumConfigConvert[T](implicit e: Enum[T], ct: ClassTag[T]): ConfigConvert[T] = {
    fromNonEmptyStringConvert(
      s => location =>
        e.decode(s).left.map(failure => CannotConvert(s, ct.runtimeClass.getSimpleName, failure.toString, location, None)),
      e.encode)
  }
}
