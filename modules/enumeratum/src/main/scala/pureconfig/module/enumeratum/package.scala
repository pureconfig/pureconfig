package pureconfig.module

import scala.reflect.ClassTag

import _root_.enumeratum._
import _root_.enumeratum.values._

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{viaNonEmptyString, viaNonEmptyStringOpt, viaStringOpt}
import pureconfig.error.CannotConvert

package object enumeratum {

  implicit def enumeratumConfigConvert[A <: EnumEntry](implicit e: Enum[A], ct: ClassTag[A]): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](e.withNameOption, _.entryName)

  implicit def enumeratumIntConfigConvert[A <: IntEnumEntry](implicit
      e: IntEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](v => e.withValueOpt(v.toInt), _.value.toString)

  implicit def enumeratumLongConfigConvert[A <: LongEnumEntry](implicit
      e: LongEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](v => e.withValueOpt(v.toLong), _.value.toString)

  implicit def enumeratumShortConfigConvert[A <: ShortEnumEntry](implicit
      e: ShortEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](v => e.withValueOpt(v.toShort), _.value.toString)

  implicit def enumeratumStringConfigConvert[A <: StringEnumEntry](implicit
      e: StringEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaStringOpt[A](v => e.withValueOpt(v), _.value.toString)

  implicit def enumeratumByteConfigConvert[A <: ByteEnumEntry](implicit
      e: ByteEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](v => e.withValueOpt(v.toByte), _.value.toString)

  implicit def enumeratumCharConfigConvert[A <: CharEnumEntry](implicit
      e: CharEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyString[A](
      s =>
        ensureOneChar(s) match {
          case Right(v) => Right(e.withValue(v))
          case Left(msg) => Left(CannotConvert(s, ct.runtimeClass.getSimpleName, msg))
        },
      _.value.toString
    )

  private val ensureOneChar: Seq[Char] => Either[String, Char] = {
    case Seq(c) => Right(c)
    case s => Left(s"""Cannot read a character value from "$s"""")
  }
}
