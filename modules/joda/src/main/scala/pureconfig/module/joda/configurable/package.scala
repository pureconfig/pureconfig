package pureconfig.module.joda

import org.joda.time._
import org.joda.time.format._
import pureconfig._
import scala.util.Try

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
    ConfigConvert.nonEmptyStringConvert[DateTime](
      s => Try(DateTime.parse(s, formatter)), formatter.print)

  def localDateConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDate] =
    ConfigConvert.nonEmptyStringConvert[LocalDate](
      s => Try(LocalDate.parse(s, formatter)), formatter.print)

  def localTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalTime] =
    ConfigConvert.nonEmptyStringConvert[LocalTime](
      s => Try(LocalTime.parse(s, formatter)), formatter.print)

  def localDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDateTime] =
    ConfigConvert.nonEmptyStringConvert[LocalDateTime](
      s => Try(LocalDateTime.parse(s, formatter)), formatter.print)

  def monthDayConfigConvert(formatter: DateTimeFormatter): ConfigConvert[MonthDay] =
    ConfigConvert.nonEmptyStringConvert[MonthDay](
      s => Try(MonthDay.parse(s, formatter)), formatter.print)

  def yearMonthConfigConvert(formatter: DateTimeFormatter): ConfigConvert[YearMonth] =
    ConfigConvert.nonEmptyStringConvert[YearMonth](
      s => Try(YearMonth.parse(s, formatter)), formatter.print)
}
