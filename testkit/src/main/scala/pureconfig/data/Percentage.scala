package pureconfig.data

import pureconfig.ConfigConvert
import pureconfig.error.CannotConvert

final case class Percentage(value: Int) {
  def toDoubleFraction: Double = value.toDouble / 100d
  def toFloatFraction: Float = value.toFloat / 100f
}

object Percentage {
  private val failConfigReadPercentage = { (s: String) =>
    Left(CannotConvert(s, "Percentage", "Percentage is a dummy type, you should not read it"))
  }

  implicit val percentageConfigWriter: ConfigConvert[Percentage] =
    ConfigConvert.viaNonEmptyString[Percentage](failConfigReadPercentage, percentage => s"${percentage.value} %")
}
