package pureconfig

trait ProductHint[T] {
  def fieldMapping: ConfigFieldMapping
}

private[pureconfig] case class ProductHintImpl[T](
  fieldMapping: ConfigFieldMapping) extends ProductHint[T]

object ProductHint {

  def apply[T](
    fieldMapping: ConfigFieldMapping = ConfigFieldMapping(CamelCase, CamelCase)): ProductHint[T] =
    ProductHintImpl[T](fieldMapping)

  implicit def default[T]: ProductHint[T] = apply()
}
