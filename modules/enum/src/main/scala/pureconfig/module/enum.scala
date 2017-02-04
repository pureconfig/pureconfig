package pureconfig.module

import _root_.enum.Enum
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{ nonEmptyStringConvert }

import scala.reflect.ClassTag
import scala.util.{ Success, Failure }

package object enum {
  implicit def enumConfigConvert[T](implicit e: Enum[T], ct: ClassTag[T]): ConfigConvert[T] = {
    nonEmptyStringConvert(
      s =>
        e.decodeOpt(s)
          .map(new Success(_))
          .getOrElse {
            val err = s"Could not interpret '$s' as a member of ${ct.runtimeClass.getSimpleName}."
            new Failure(new IllegalArgumentException(err))
          },
      e.encode)
  }
}
