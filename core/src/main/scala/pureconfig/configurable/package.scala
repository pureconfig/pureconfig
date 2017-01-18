package pureconfig

import scala.util.Try
import java.time.{ LocalDate, LocalDateTime, LocalTime }
import java.time.format.DateTimeFormatter

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
    ConfigConvert.nonEmptyStringConvert[LocalDate](
      s => Try(LocalDate.parse(s, formatter)), _.format(formatter))

  def localTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalTime] =
    ConfigConvert.nonEmptyStringConvert[LocalTime](
      s => Try(LocalTime.parse(s, formatter)), _.format(formatter))

  def localDateTimeConfigConvert(formatter: DateTimeFormatter): ConfigConvert[LocalDateTime] =
    ConfigConvert.nonEmptyStringConvert[LocalDateTime](
      s => Try(LocalDateTime.parse(s, formatter)), _.format(formatter))
}
