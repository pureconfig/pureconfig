package pureconfig.module.joda.configurable

import org.joda.time._
import org.joda.time.format.{DateTimeFormat, ISOPeriodFormat, PeriodFormatter}
import pureconfig.BaseSuite
import pureconfig.module.joda.arbitrary._

class ConfigurableSuite extends BaseSuite {

  val isoFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ")
  implicit val dateTimeInstance = dateTimeConfigConvert(isoFormatter)
  checkArbitrary[DateTime]

  val timeFormatter = DateTimeFormat.forPattern("HH:mm:ss.SSS")
  implicit val localTimeInstance = localTimeConfigConvert(timeFormatter)
  checkArbitrary[LocalTime]

  val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  implicit val localDateInstance = localDateConfigConvert(dateFormatter)
  checkArbitrary[LocalDate]

  val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  implicit val localDateTimeInstance = localDateTimeConfigConvert(dateTimeFormatter)
  checkArbitrary[LocalDateTime]

  val monthDayFormat = DateTimeFormat.forPattern("MM-dd")
  implicit val monthDayInstance = monthDayConfigConvert(monthDayFormat)
  checkArbitrary[MonthDay]

  val yearMonthFormat = DateTimeFormat.forPattern("yyyy-MM")
  implicit val yearMonthInstance = yearMonthConfigConvert(yearMonthFormat)
  checkArbitrary[YearMonth]

  val periodFormatter: PeriodFormatter = ISOPeriodFormat.standard()
  implicit val periodInstance = periodConfigConvert(periodFormatter)
  checkArbitrary[Period]
}
