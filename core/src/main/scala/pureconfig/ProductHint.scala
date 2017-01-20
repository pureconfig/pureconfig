package pureconfig

trait ProductHint[T] {
  def fieldMapping: ConfigFieldMapping
  def useDefaultArgs: Boolean
}

private[pureconfig] case class ProductHintImpl[T](
  fieldMapping: ConfigFieldMapping,
  useDefaultArgs: Boolean) extends ProductHint[T]

object ProductHint {

  def apply[T](
    fieldMapping: ConfigFieldMapping = ConfigFieldMapping(CamelCase, CamelCase),
    useDefaultArgs: Boolean = true): ProductHint[T] =
    ProductHintImpl[T](fieldMapping, useDefaultArgs)

  implicit def default[T]: ProductHint[T] = apply()
}
