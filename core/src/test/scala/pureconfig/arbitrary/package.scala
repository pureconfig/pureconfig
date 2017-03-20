package pureconfig

import org.scalacheck.Arbitrary

import pureconfig.gen._

package object arbitrary {

  implicit val arbDuration = Arbitrary(genDuration)
  implicit val arbFiniteDuration = Arbitrary(genFiniteDuration)
  implicit val arbInstant = Arbitrary(genInstant)
  implicit val arbZoneId = Arbitrary(genZoneId)
  implicit val arbZoneOffset = Arbitrary(genZoneOffset)
  implicit val arbPeriod = Arbitrary(genPeriod)
  implicit val arbYear = Arbitrary(genYear)
  implicit val arbUUID = Arbitrary(genUUID)
  implicit val arbPath = Arbitrary(genPath)
  implicit val arbPercentage = Arbitrary(genPercentage)
  implicit val arbJodaDateTime = Arbitrary(genJodaDateTime)
}
