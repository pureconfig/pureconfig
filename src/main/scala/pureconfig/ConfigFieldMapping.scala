package pureconfig

trait ConfigFieldMapping[T] {
  def toConfigField(fieldName: String): String
}

class NoopConfigFieldMapping[T] extends ConfigFieldMapping[T] {
  def toConfigField(fieldName: String): String = fieldName
}

class WordDelimiterConfigFieldMapping[T](
    typeFieldDelimiter: WordDelimiter,
    configFieldDelimiter: WordDelimiter) extends ConfigFieldMapping[T] {
  def toConfigField(fieldName: String): String =
    (typeFieldDelimiter.toTokens _ andThen configFieldDelimiter.fromTokens _)(fieldName)
}

object ConfigFieldMapping extends LowPriorityConfigFieldMappingImplicits {
  def apply[T](implicit mapping: ConfigFieldMapping[T]): ConfigFieldMapping[T] = mapping
}

trait LowPriorityConfigFieldMappingImplicits {
  implicit def configFieldMapping[T]: ConfigFieldMapping[T] = new NoopConfigFieldMapping[T]
}
