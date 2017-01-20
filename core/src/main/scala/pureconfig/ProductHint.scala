package pureconfig

trait ProductHint[T] {
  def fieldMapping: ConfigFieldMapping
  def useDefaultArgs: Boolean
  def allowUnknownKeys: Boolean
}

private[pureconfig] case class ProductHintImpl[T](
  fieldMapping: ConfigFieldMapping,
  useDefaultArgs: Boolean,
  allowUnknownKeys: Boolean) extends ProductHint[T]

object ProductHint {

  def apply[T](
    fieldMapping: ConfigFieldMapping = ConfigFieldMapping(CamelCase, CamelCase),
    useDefaultArgs: Boolean = true,
    allowUnknownKeys: Boolean = true): ProductHint[T] =
    ProductHintImpl[T](fieldMapping, useDefaultArgs, allowUnknownKeys)

  implicit def default[T]: ProductHint[T] = apply()
}
