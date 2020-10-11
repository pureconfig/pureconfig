package pureconfig

import org.scalacheck.{Arbitrary, Gen}
import pureconfig.gen._

package object arbitrary {

  implicit val arbDuration = Arbitrary(genDuration)
  implicit val arbJavaDuration = Arbitrary(genJavaDuration)
  implicit val arbFiniteDuration = Arbitrary(genFiniteDuration)
  implicit val arbInstant = Arbitrary(genInstant)
  implicit val arbPeriod = Arbitrary(genPeriod)
  implicit val arbYear = Arbitrary(genYear)
  implicit val arbUUID = Arbitrary(Gen.uuid)
  implicit val arbPath = Arbitrary(genPath)
  implicit val arbFile = Arbitrary(genFile)
  implicit val arbPercentage = Arbitrary(genPercentage)
  implicit val arbJavaBigDecimal = Arbitrary(genJavaBigDecimal)
  implicit val arbJavaBigInteger = Arbitrary(genBigInt)

  implicit val arbLocalTime = Arbitrary(genLocalTime)
  implicit val arbLocalDate = Arbitrary(genLocalDate)
  implicit val arbLocalDateTime = Arbitrary(genLocalDateTime)
  implicit val arbMonthDay = Arbitrary(genMonthDay)
  implicit val arbZoneOffset = Arbitrary(genZoneOffset)
  implicit val arbOffsetDateTime = Arbitrary(genOffsetDateTime)
  implicit val arbOffsetTime = Arbitrary(genOffsetTime)
  implicit val arbYearMonth = Arbitrary(genYearMonth)
  implicit val arbZoneId = Arbitrary(genZoneId)
  implicit val arbZonedDateTime = Arbitrary(genZonedDateTime)
}
