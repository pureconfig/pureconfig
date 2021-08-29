package pureconfig

import java.time._
import java.time.format.DateTimeFormatter

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

import com.typesafe.config.ConfigValueFactory

import pureconfig.ConfigConvert.viaNonEmptyString
import pureconfig.ConvertHelpers.catchReadError
import pureconfig.error.FailureReason

/** Provides methods that create [[ConfigConvert]] instances from a set of parameters used to configure the instances.
  *
  * The result of calling one of the methods can be assigned to an `implicit val` so that `pureconfig` will be able to
  * use it:
  * {{{
  *   implicit val localDateConfigConvert = makeLocalDateConfigConvert(DateTimeFormatter.ISO_TIME)
  * }}}
  *
  * @example
  *   we cannot provide a [[ConfigConvert]] for [[java.time.LocalDate]] because traditionally there are many different
  *   [[java.time.format.DateTimeFormatter]] s to parse a [[java.time.LocalDate]] from a [[java.lang.String]]. This
  *   package provides a method that takes an input [[java.time.format.DateTimeFormatter]] and returns a
  *   [[ConfigConvert]] for [[java.time.LocalDate]] which will use that [[java.time.format.DateTimeFormatter]] to parse
  *   a [[java.time.LocalDate]].
  */
package object configurable {

  def localDateConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDate] =
    viaNonEmptyString[LocalDate](catchReadError(LocalDate.parse(_, formatter)), _.format(formatter))

  def localTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalTime] =
    viaNonEmptyString[LocalTime](catchReadError(LocalTime.parse(_, formatter)), _.format(formatter))

  def localDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDateTime] =
    viaNonEmptyString[LocalDateTime](catchReadError(LocalDateTime.parse(_, formatter)), _.format(formatter))

  def monthDayConfigConvert(formatter: DateTimeFormatter): ConfigConvert[MonthDay] =
    viaNonEmptyString[MonthDay](catchReadError(MonthDay.parse(_, formatter)), _.format(formatter))

  def offsetDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[OffsetDateTime] =
    viaNonEmptyString[OffsetDateTime](catchReadError(OffsetDateTime.parse(_, formatter)), _.format(formatter))

  def offsetTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[OffsetTime] =
    viaNonEmptyString[OffsetTime](catchReadError(OffsetTime.parse(_, formatter)), _.format(formatter))

  def yearMonthConfigConvert(formatter: DateTimeFormatter): ConfigConvert[YearMonth] =
    viaNonEmptyString[YearMonth](catchReadError(YearMonth.parse(_, formatter)), _.format(formatter))

  def zonedDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[ZonedDateTime] =
    viaNonEmptyString[ZonedDateTime](catchReadError(ZonedDateTime.parse(_, formatter)), _.format(formatter))

  def genericMapReader[K, V](
      keyParser: String => Either[FailureReason, K]
  )(implicit readerV: ConfigReader[V]): ConfigReader[Map[K, V]] =
    ConfigReader.fromCursor { cursor =>
      cursor.asMap.flatMap { map =>
        map.foldLeft[ConfigReader.Result[Map[K, V]]](Right(Map.empty)) { case (acc, (key, valueCursor)) =>
          val eitherKeyOrError = cursor.scopeFailure(keyParser(key))
          val eitherValueOrError = readerV.from(valueCursor)
          ConfigReader.Result.zipWith(acc, ConfigReader.Result.zipWith(eitherKeyOrError, eitherValueOrError)(_ -> _))(
            _ + _
          )
        }
      }
    }

  def genericMapWriter[K, V](
      keyFormatter: K => String
  )(implicit writerV: ConfigWriter[V]): ConfigWriter[Map[K, V]] =
    ConfigWriter.fromFunction[Map[K, V]](map =>
      ConfigValueFactory.fromMap(map.map { case (key, value) =>
        keyFormatter(key) -> writerV.to(value)
      }.asJava)
    )

  def genericJavaEnumReader[A <: java.lang.Enum[A]](
      transformValue: String => String
  )(implicit tag: ClassTag[A]): ConfigReader[A] =
    ConfigReader.fromString(catchReadError(s => {
      val enumClass = tag.runtimeClass.asInstanceOf[Class[A]]
      Enum.valueOf(enumClass, transformValue(s))
    }))
}
