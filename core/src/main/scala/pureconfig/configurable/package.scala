package pureconfig

import scala.util.Try
import java.time.{ LocalDate, LocalDateTime, LocalTime }
import java.time.format.DateTimeFormatter

package object configurable {

  def makeLocalDateInstance(formatter: DateTimeFormatter): ConfigConvert[LocalDate] =
    ConfigConvert.nonEmptyStringConvert[LocalDate](
      s => Try(LocalDate.parse(s, formatter)), _.format(formatter))

  def makeLocalTimeInstance(formatter: DateTimeFormatter): ConfigConvert[LocalTime] =
    ConfigConvert.nonEmptyStringConvert[LocalTime](
      s => Try(LocalTime.parse(s, formatter)), _.format(formatter))

  def makeLocalDateTimeInstance(formatter: DateTimeFormatter): ConfigConvert[LocalDateTime] =
    ConfigConvert.nonEmptyStringConvert[LocalDateTime](
      s => Try(LocalDateTime.parse(s, formatter)), _.format(formatter))
}
