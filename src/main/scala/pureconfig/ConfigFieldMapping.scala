package pureconfig

trait ConfigFieldMapping[T] extends (String => String) {
  def apply(fieldName: String): String
}

class IdentityConfigFieldMapping[T] extends ConfigFieldMapping[T] {
  def apply(fieldName: String): String = fieldName
}

class WordDelimiterConfigFieldMapping[T](
    typeFieldDelimiter: WordDelimiter,
    configFieldDelimiter: WordDelimiter) extends ConfigFieldMapping[T] {
  def apply(fieldName: String): String =
    (typeFieldDelimiter.toTokens _ andThen configFieldDelimiter.fromTokens _)(fieldName)
}

object ConfigFieldMapping extends LowPriorityConfigFieldMappingImplicits {
  def apply[T](implicit mapping: ConfigFieldMapping[T]): ConfigFieldMapping[T] = mapping
}

trait LowPriorityConfigFieldMappingImplicits {
  implicit def configFieldMapping[T]: ConfigFieldMapping[T] = new IdentityConfigFieldMapping[T]
}
