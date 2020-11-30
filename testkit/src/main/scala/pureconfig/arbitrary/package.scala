package pureconfig

import java.io.File
import java.math.BigInteger
import java.nio.file.Path
import java.time.{Duration => JavaDuration, _}
import java.util.UUID

import scala.concurrent.duration.{Duration, FiniteDuration}

import org.scalacheck.{Arbitrary, Gen}
import pureconfig.data.Percentage
import pureconfig.gen._

package object arbitrary {

  implicit val arbDuration: Arbitrary[Duration] = Arbitrary(genDuration)
  implicit val arbJavaDuration: Arbitrary[JavaDuration] = Arbitrary(genJavaDuration)
  implicit val arbFiniteDuration: Arbitrary[FiniteDuration] = Arbitrary(genFiniteDuration)
  implicit val arbInstant: Arbitrary[Instant] = Arbitrary(genInstant)
  implicit val arbPeriod: Arbitrary[Period] = Arbitrary(genPeriod)
  implicit val arbYear: Arbitrary[Year] = Arbitrary(genYear)
  implicit val arbUUID: Arbitrary[UUID] = Arbitrary(Gen.uuid)
  implicit val arbPath: Arbitrary[Path] = Arbitrary(genPath)
  implicit val arbFile: Arbitrary[File] = Arbitrary(genFile)
  implicit val arbPercentage: Arbitrary[Percentage] = Arbitrary(genPercentage)
  implicit val arbJavaBigDecimal: Arbitrary[java.math.BigDecimal] = Arbitrary(genJavaBigDecimal)
  implicit val arbJavaBigInteger: Arbitrary[BigInteger] = Arbitrary(genBigInt)

  implicit val arbLocalTime: Arbitrary[LocalTime] = Arbitrary(genLocalTime)
  implicit val arbLocalDate: Arbitrary[LocalDate] = Arbitrary(genLocalDate)
  implicit val arbLocalDateTime: Arbitrary[LocalDateTime] = Arbitrary(genLocalDateTime)
  implicit val arbMonthDay: Arbitrary[MonthDay] = Arbitrary(genMonthDay)
  implicit val arbZoneOffset: Arbitrary[ZoneOffset] = Arbitrary(genZoneOffset)
  implicit val arbOffsetDateTime: Arbitrary[OffsetDateTime] = Arbitrary(genOffsetDateTime)
  implicit val arbOffsetTime: Arbitrary[OffsetTime] = Arbitrary(genOffsetTime)
  implicit val arbYearMonth: Arbitrary[YearMonth] = Arbitrary(genYearMonth)
  implicit val arbZoneId: Arbitrary[ZoneId] = Arbitrary(genZoneId)
  implicit val arbZonedDateTime: Arbitrary[ZonedDateTime] = Arbitrary(genZonedDateTime)
}
