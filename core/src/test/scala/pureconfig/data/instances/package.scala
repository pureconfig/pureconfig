package pureconfig.data

import pureconfig.ConfigConvert
import pureconfig.error.{ CannotConvert, ConfigValueLocation }

package object instances {

  private val failConfigReadPercentage =
    (s: String) => (l: Option[ConfigValueLocation]) =>
      Left(CannotConvert(s, "Percentage", "Percentage is a dummy type, you should not read it", l))

  implicit val percentageConfigWriter =
    ConfigConvert.fromNonEmptyStringConvert[Percentage](failConfigReadPercentage, percentage => s"${percentage.value} %")
}
