package pureconfig.data

final case class Percentage(value: Int) {
  def toDoubleFraction: Double = value.toDouble / 100D
  def toFloatFraction: Float = value.toFloat / 100f
}