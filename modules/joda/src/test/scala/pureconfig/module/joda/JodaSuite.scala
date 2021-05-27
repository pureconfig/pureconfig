package pureconfig.module.joda

import org.joda.time._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import pureconfig.BaseSuite
import pureconfig.module.joda.arbitrary._

class JodaSuite extends BaseSuite {

  checkArbitrary[Instant]

  checkArbitrary[Interval]

  checkArbitrary[Duration]

  checkArbitrary[DateTimeZone]

  checkReadString[DateTimeFormatter](
    "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ" -> DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ")
  )
}
