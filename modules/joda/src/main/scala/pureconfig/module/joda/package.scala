package pureconfig.module

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTimeZone, Duration, Instant, Interval}

import pureconfig.ConfigConvert.{catchReadError, viaNonEmptyString}
import pureconfig.{ConfigConvert, ConfigReader}

package object joda {
  implicit def instantConfigConvert: ConfigConvert[Instant] =
    ConfigConvert[Long].xmap(new Instant(_), _.getMillis)

  implicit def intervalConfigConvert: ConfigConvert[Interval] =
    viaNonEmptyString[Interval](catchReadError(Interval.parseWithOffset), _.toString)

  implicit def durationConfigConvert: ConfigConvert[Duration] =
    viaNonEmptyString[Duration](catchReadError(Duration.parse), _.toString)

  implicit def dateTimeFormatterConfigConvert: ConfigReader[DateTimeFormatter] =
    ConfigReader.fromNonEmptyString[DateTimeFormatter](catchReadError(DateTimeFormat.forPattern))

  implicit def dateTimeZoneConfigConvert: ConfigConvert[DateTimeZone] =
    viaNonEmptyString[DateTimeZone](catchReadError(DateTimeZone.forID), _.getID)
}
