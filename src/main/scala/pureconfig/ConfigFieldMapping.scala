package pureconfig

trait ConfigFieldMapping[T] extends (String => String) {
  def apply(fieldName: String): String
}

object ConfigFieldMapping extends LowPriorityConfigFieldMappingImplicits {
  def apply[T](implicit mapping: ConfigFieldMapping[T]): ConfigFieldMapping[T] = mapping

  /**
   * Creates a ConfigFieldMapping from the provided function, mapping names in
   * the object that will receive config values to names in the source
   * configuration.
   *
   * @param f a function that maps names in the object that will receive config
   *        values to names in the source configuration
   * @return a ConfigFieldMapping created from the provided function.
   */
  def apply[T](f: String => String): ConfigFieldMapping[T] = new ConfigFieldMapping[T] {
    def apply(fieldName: String): String = f(fieldName)
  }

  /**
   * Creates a ConfigFieldMapping according to the naming conventions specified
   * both for the object that will receive config values and for the source
   * configuration.
   *
   * @param typeFieldConvention naming convention used by the fields of the
   *        object which will receive config values
   * @param configFieldConvention naming convention used in the source
   *        configuration (e.g. the config file)
   * @return a ConfigFieldMapping created according to the provided naming
   *         conventions.
   */
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
