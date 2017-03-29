package pureconfig

import java.time._
import java.time.format.DateTimeFormatter

import pureconfig.ConfigConvert.{ catchReadError, viaNonEmptyString }

/**
 * Provides methods that create [[ConfigConvert]] instances from a set of parameters used to configure the instances.
 *
 * The result of calling one of the methods can be assigned to an `implicit val` so that `pureconfig` will be able to
 * use it:
 * {{{
 *   implicit val localDateConfigConvert = makeLocalDateConfigConvert(DateTimeFormatter.ISO_TIME)
 * }}}
 *
 * @example we cannot provide a [[ConfigConvert]] for [[java.time.LocalDate]] because traditionally there are many different
 * [[java.time.format.DateTimeFormatter]]s to parse a [[java.time.LocalDate]] from a [[java.lang.String]]. This package
 * provides a method that takes an input [[java.time.format.DateTimeFormatter]] and returns a [[ConfigConvert]] for
 * [[java.time.LocalDate]] which will use that [[java.time.format.DateTimeFormatter]] to parse a [[java.time.LocalDate]].
 */
package object configurable {

  def localDateConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDate] =
    viaNonEmptyString[LocalDate](
      catchReadError(LocalDate.parse(_, formatter)), _.format(formatter))

  def localTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalTime] =
    viaNonEmptyString[LocalTime](
      catchReadError(LocalTime.parse(_, formatter)), _.format(formatter))

  def localDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDateTime] =
    viaNonEmptyString[LocalDateTime](
      catchReadError(LocalDateTime.parse(_, formatter)), _.format(formatter))

  def monthDayConfigConvert(formatter: DateTimeFormatter): ConfigConvert[MonthDay] =
    viaNonEmptyString[MonthDay](
      catchReadError(MonthDay.parse(_, formatter)), _.format(formatter))

  def offsetDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[OffsetDateTime] =
    viaNonEmptyString[OffsetDateTime](
      catchReadError(OffsetDateTime.parse(_, formatter)), _.format(formatter))

  def offsetTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[OffsetTime] =
    viaNonEmptyString[OffsetTime](
      catchReadError(OffsetTime.parse(_, formatter)), _.format(formatter))

  def yearMonthConfigConvert(formatter: DateTimeFormatter): ConfigConvert[YearMonth] =
    viaNonEmptyString[YearMonth](
      catchReadError(YearMonth.parse(_, formatter)), _.format(formatter))

  def zonedDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[ZonedDateTime] =
    viaNonEmptyString[ZonedDateTime](
      catchReadError(ZonedDateTime.parse(_, formatter)), _.format(formatter))
}
