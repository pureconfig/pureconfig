package pureconfig.module

import _root_.enumeratum._
import _root_.enumeratum.values._
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.{ fromNonEmptyStringConvert, fromStringConvert }
import pureconfig.error.{ CannotConvert, ConfigReaderFailure }

import scala.reflect.ClassTag

package object enumeratum {

  private[this] def optionToCannotConvert[T](name: String, f: String => Option[T])(implicit ct: ClassTag[T]): Either[ConfigReaderFailure, T] =
    f(name) match {
      case None => Left(CannotConvert(name, ct.runtimeClass.getSimpleName, ""))
      case Some(t) => Right(t)
    }

  implicit def enumeratumConfigConvert[A <: EnumEntry](implicit enum: Enum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvert[A](optionToCannotConvert(_, enum.withNameOption), _.entryName)

  implicit def enumeratumIntConfigConvert[A <: IntEnumEntry](implicit enum: IntEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvert[A](optionToCannotConvert(_, s => enum.withValueOpt(s.toInt)), _.value.toString)

  implicit def enumeratumLongConfigConvert[A <: LongEnumEntry](implicit enum: LongEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvert[A](optionToCannotConvert(_, v => enum.withValueOpt(v.toLong)), _.value.toString)

  implicit def enumeratumShortConfigConvert[A <: ShortEnumEntry](implicit enum: ShortEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvert[A](optionToCannotConvert(_, v => enum.withValueOpt(v.toShort)), _.value.toString)

  implicit def enumeratumStringConfigConvert[A <: StringEnumEntry](implicit enum: StringEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromStringConvert[A](optionToCannotConvert(_, v => enum.withValueOpt(v)), _.value.toString)

  implicit def enumeratumByteConfigConvert[A <: ByteEnumEntry](implicit enum: ByteEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvert[A](optionToCannotConvert(_, v => enum.withValueOpt(v.toByte)), _.value.toString)

  implicit def enumeratumCharConfigConvert[A <: CharEnumEntry](implicit enum: CharEnum[A], ct: ClassTag[A]): ConfigConvert[A] =
    fromNonEmptyStringConvert[A](
      s => ensureOneChar(s) match {
        case Right(v) => Right(enum.withValue(v))
        case Left(msg) => Left(CannotConvert(s, ct.runtimeClass.getSimpleName, msg))
      },
      _.value.toString)

  private val ensureOneChar: Seq[Char] => Either[String, Char] = {
    case Seq(c) => Right(c)
    case s => Left(s"""Cannot read a character value from "$s"""")
  }
}
