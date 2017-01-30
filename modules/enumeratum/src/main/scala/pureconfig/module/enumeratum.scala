package pureconfig.module

import _root_.enumeratum.values._
import _root_.enumeratum._
import pureconfig.ConfigConvert

import scala.util.Try

package object enumeratum {
  implicit def enumeratumConfigConvert[A <: EnumEntry](implicit enum: Enum[A]): ConfigConvert[A] =
    ConfigConvert.stringConvert[A](name => Try(enum.withName(name)), _.toString)

  implicit def enumeratumIntConfigConvert[A <: IntEnumEntry](implicit enum: IntEnum[A]): ConfigConvert[A] =
    ConfigConvert.stringConvert[A](v => Try(enum.withValue(v.toInt)), _.toString)

  implicit def enumeratumLongConfigConvert[A <: LongEnumEntry](implicit enum: LongEnum[A]): ConfigConvert[A] =
    ConfigConvert.stringConvert[A](v => Try(enum.withValue(v.toLong)), _.toString)

  implicit def enumeratumShortConfigConvert[A <: ShortEnumEntry](implicit enum: ShortEnum[A]): ConfigConvert[A] =
    ConfigConvert.stringConvert[A](v => Try(enum.withValue(v.toShort)), _.toString)

  implicit def enumeratumStringConfigConvert[A <: StringEnumEntry](implicit enum: StringEnum[A]): ConfigConvert[A] =
    ConfigConvert.stringConvert[A](v => Try(enum.withValue(v)), _.toString)

  implicit def enumeratumByteConfigConvert[A <: ByteEnumEntry](implicit enum: ByteEnum[A]): ConfigConvert[A] =
    ConfigConvert.stringConvert[A](v => Try(enum.withValue(v.toByte)), _.toString)

  implicit def enumeratumCharConfigConvert[A <: CharEnumEntry](implicit enum: CharEnum[A]): ConfigConvert[A] =
    ConfigConvert.stringConvert[A](v => Try(enum.withValue(v.charAt(0))), _.toString)
}
