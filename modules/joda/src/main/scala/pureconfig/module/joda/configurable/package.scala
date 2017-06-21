package pureconfig.module.joda

import org.joda.time._
import org.joda.time.format._
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert._

/**
 * Provides methods that create [[ConfigConvert]] instances from a set of parameters used to configure the instances.
 *
 * The result of calling one of the methods can be assigned to an `implicit val` so that `pureconfig` will be able to
 * use it:
 * {{{
 *   implicit val localDateConfigConvert = makeLocalDateConfigConvert(ISODateTimeFormat)
 * }}}
 *
 * @example we cannot provide a [[ConfigConvert]] for [[org.joda.time.LocalDate]] because traditionally there are many different
 * [[org.joda.time.format.DateTimeFormatter]]s to parse a [[org.joda.time.LocalDate]] from a [[java.lang.String]]. This package
 * provides a method that takes an input [[org.joda.time.format.DateTimeFormatter]] and returns a [[ConfigConvert]] for
 * [[org.joda.time.LocalDate]] which will use that [[org.joda.time.format.DateTimeFormatter]] to parse a [[org.joda.time.LocalDate]].
 */
package object configurable {
  def dateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[DateTime] =
    viaNonEmptyString[DateTime](
      catchReadError(DateTime.parse(_, formatter)), formatter.print)

  def localDateConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDate] =
    viaNonEmptyString[LocalDate](
      catchReadError(LocalDate.parse(_, formatter)), formatter.print)

  def localTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalTime] =
    viaNonEmptyString[LocalTime](
      catchReadError(LocalTime.parse(_, formatter)), formatter.print)

  def localDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDateTime] =
    viaNonEmptyString[LocalDateTime](
      catchReadError(LocalDateTime.parse(_, formatter)), formatter.print)

  def monthDayConfigConvert(formatter: DateTimeFormatter): ConfigConvert[MonthDay] =
    viaNonEmptyString[MonthDay](
      catchReadError(MonthDay.parse(_, formatter)), formatter.print)

  def yearMonthConfigConvert(formatter: DateTimeFormatter): ConfigConvert[YearMonth] =
    viaNonEmptyString[YearMonth](
      catchReadError(YearMonth.parse(_, formatter)), formatter.print)

  def periodConfigConvert(formatter: PeriodFormatter): ConfigConvert[Period] =
    viaNonEmptyString[Period](
      catchReadError(Period.parse(_, formatter)), formatter.print)
}
