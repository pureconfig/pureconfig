package pureconfig

trait ConfigFieldMapping[T] extends (String => String) {
  def apply(fieldName: String): String
}

object ConfigFieldMapping extends LowPriorityConfigFieldMappingImplicits {
  def apply[T](implicit mapping: ConfigFieldMapping[T]): ConfigFieldMapping[T] = mapping
  def apply[T](f: String => String): ConfigFieldMapping[T] = new ConfigFieldMapping[T] {
    def apply(fieldName: String): String = f(fieldName)
  }
  def apply[T](typeFieldConvention: NamingConvention, configFieldConvention: NamingConvention): ConfigFieldMapping[T] = {
    if (typeFieldConvention == configFieldConvention) {
      apply(identity(_))
    } else {
      apply(typeFieldConvention.toTokens _ andThen configFieldConvention.fromTokens _)
    }
  }

  def default[T]: ConfigFieldMapping[T] = apply(CamelCase, CamelCase)
}

trait LowPriorityConfigFieldMappingImplicits {
  implicit def configFieldMapping[T]: ConfigFieldMapping[T] = ConfigFieldMapping.default[T]
}
