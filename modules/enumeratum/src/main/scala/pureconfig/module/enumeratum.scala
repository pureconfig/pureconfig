package pureconfig.module

import _root_.enumeratum._
import _root_.enumeratum.values._
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{ nonEmptyStringConvert, stringConvert }

import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }

package object enumeratum {
  implicit def enumeratumConfigConvert[A <: EnumEntry](implicit enum: Enum[A], ct: ClassTag[A]): ConfigConvert[A] =
    nonEmptyStringConvert[A](name => Try(enum.withName(name)), _.entryName)

  implicit def enumeratumIntConfigConvert[A <: IntEnumEntry](implicit enum: IntEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    nonEmptyStringConvert[A](v => Try(enum.withValue(v.toInt)), _.value.toString)

  implicit def enumeratumLongConfigConvert[A <: LongEnumEntry](implicit enum: LongEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    nonEmptyStringConvert[A](v => Try(enum.withValue(v.toLong)), _.value.toString)

  implicit def enumeratumShortConfigConvert[A <: ShortEnumEntry](implicit enum: ShortEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    nonEmptyStringConvert[A](v => Try(enum.withValue(v.toShort)), _.value.toString)

  implicit def enumeratumStringConfigConvert[A <: StringEnumEntry](implicit enum: StringEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    stringConvert[A](v => Try(enum.withValue(v)), _.value.toString)

  implicit def enumeratumByteConfigConvert[A <: ByteEnumEntry](implicit enum: ByteEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    nonEmptyStringConvert[A](v => Try(enum.withValue(v.toByte)), _.value.toString)

  implicit def enumeratumCharConfigConvert[A <: CharEnumEntry](implicit enum: CharEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    nonEmptyStringConvert[A](ensureOneChar(_).map(enum.withValue), _.value.toString)

  private val ensureOneChar: Seq[Char] => Try[Char] = {
    case Seq(c) => Success(c)
    case s => Failure(new IllegalArgumentException(s"""Cannot read a character value from "$s""""))
  }
}
