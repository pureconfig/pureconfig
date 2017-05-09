package pureconfig

import org.scalacheck.{ Arbitrary, Gen }
import pureconfig.gen._

package object arbitrary {

  implicit val arbDuration = Arbitrary(genDuration)
  implicit val arbJavaDuration = Arbitrary(genJavaDuration)
  implicit val arbFiniteDuration = Arbitrary(genFiniteDuration)
  implicit val arbInstant = Arbitrary(genInstant)
  implicit val arbZoneId = Arbitrary(genZoneId)
  implicit val arbZoneOffset = Arbitrary(genZoneOffset)
  implicit val arbPeriod = Arbitrary(genPeriod)
  implicit val arbYear = Arbitrary(genYear)
  implicit val arbUUID = Arbitrary(Gen.uuid)
  implicit val arbPath = Arbitrary(genPath)
  implicit val arbPercentage = Arbitrary(genPercentage)
  implicit val arbJodaDateTime = Arbitrary(genJodaDateTime)
}
