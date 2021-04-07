package pureconfig.module.joda.configurable

import org.joda.time._
import org.joda.time.format.{DateTimeFormat, ISOPeriodFormat, PeriodFormatter}

import pureconfig.module.joda.arbitrary._
import pureconfig.{BaseSuite, ConfigConvert}

class ConfigurableSuite extends BaseSuite {

  val isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ")
  implicit val dateTimeInstance: ConfigConvert[DateTime] = dateTimeConfigConvert(isoFormatter)
  checkArbitrary[DateTime]

  val timeFormatter = DateTimeFormat.forPattern("HH:mm:ss.SSS")
  implicit val localTimeInstance: ConfigConvert[LocalTime] = localTimeConfigConvert(timeFormatter)
  checkArbitrary[LocalTime]

  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  implicit val localDateInstance: ConfigConvert[LocalDate] = localDateConfigConvert(dateFormatter)
  checkArbitrary[LocalDate]

  val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  implicit val localDateTimeInstance: ConfigConvert[LocalDateTime] = localDateTimeConfigConvert(dateTimeFormatter)
  checkArbitrary[LocalDateTime]

  val monthDayFormat = DateTimeFormat.forPattern("MM-dd")
  implicit val monthDayInstance: ConfigConvert[MonthDay] = monthDayConfigConvert(monthDayFormat)
  checkArbitrary[MonthDay]

  val yearMonthFormat = DateTimeFormat.forPattern("yyyy-MM")
  implicit val yearMonthInstance: ConfigConvert[YearMonth] = yearMonthConfigConvert(yearMonthFormat)
  checkArbitrary[YearMonth]

  val periodFormatter: PeriodFormatter = ISOPeriodFormat.standard()
  implicit val periodInstance: ConfigConvert[Period] = periodConfigConvert(periodFormatter)
  checkArbitrary[Period]
}
