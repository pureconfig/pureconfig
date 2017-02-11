package pureconfig

import java.time._
import java.time.format.DateTimeFormatter

import ConfigConvert.{ fromNonEmptyStringConvert, catchReadError }

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
    fromNonEmptyStringConvert[LocalDate](
      catchReadError(LocalDate.parse(_, formatter)), _.format(formatter))

  def localTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalTime] =
    fromNonEmptyStringConvert[LocalTime](
      catchReadError(LocalTime.parse(_, formatter)), _.format(formatter))

  def localDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDateTime] =
    fromNonEmptyStringConvert[LocalDateTime](
      catchReadError(LocalDateTime.parse(_, formatter)), _.format(formatter))

  def monthDayConfigConvert(formatter: DateTimeFormatter): ConfigConvert[MonthDay] =
    fromNonEmptyStringConvert[MonthDay](
      catchReadError(MonthDay.parse(_, formatter)), _.format(formatter))

  def offsetDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[OffsetDateTime] =
    fromNonEmptyStringConvert[OffsetDateTime](
      catchReadError(OffsetDateTime.parse(_, formatter)), _.format(formatter))

  def offsetTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[OffsetTime] =
    fromNonEmptyStringConvert[OffsetTime](
      catchReadError(OffsetTime.parse(_, formatter)), _.format(formatter))

  def yearMonthConfigConvert(formatter: DateTimeFormatter): ConfigConvert[YearMonth] =
    fromNonEmptyStringConvert[YearMonth](
      catchReadError(YearMonth.parse(_, formatter)), _.format(formatter))

  def zonedDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[ZonedDateTime] =
    fromNonEmptyStringConvert[ZonedDateTime](
      catchReadError(ZonedDateTime.parse(_, formatter)), _.format(formatter))
}
