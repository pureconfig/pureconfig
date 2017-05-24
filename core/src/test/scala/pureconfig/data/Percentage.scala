package pureconfig.data

import pureconfig.ConfigConvert
import pureconfig.error.{ CannotConvert, ConfigValueLocation }

final case class Percentage(value: Int) {
  def toDoubleFraction: Double = value.toDouble / 100D
  def toFloatFraction: Float = value.toFloat / 100f
}

object Percentage {
  private val failConfigReadPercentage =
    (s: String) => (l: Option[ConfigValueLocation]) =>
      Left(CannotConvert(s, "Percentage", "Percentage is a dummy type, you should not read it", l, ""))

  implicit val percentageConfigWriter =
    ConfigConvert.viaNonEmptyString[Percentage](failConfigReadPercentage, percentage => s"${percentage.value} %")
}
