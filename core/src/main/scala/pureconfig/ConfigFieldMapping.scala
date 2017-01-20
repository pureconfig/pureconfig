package pureconfig

trait ConfigFieldMapping extends (String => String) {
  def apply(fieldName: String): String
}

object ConfigFieldMapping {
  /**
   * Creates a ConfigFieldMapping from the provided function, mapping names in
   * the object that will receive config values to names in the configuration
   * file.
   *
   * @param f a function that maps names in the object that will receive config
   *        values to names in the configuration file
   * @return a ConfigFieldMapping created from the provided function.
   */
  def apply(f: String => String): ConfigFieldMapping = new ConfigFieldMapping {
    def apply(fieldName: String): String = f(fieldName)
  }

  /**
   * Creates a ConfigFieldMapping according to the naming conventions specified
   * both for the object that will receive config values and for the
   * configuration file.
   *
   * @param typeFieldConvention naming convention used by the fields of the
   *        object which will receive config values
   * @param configFieldConvention naming convention used in the configuration
   *        file
   * @return a ConfigFieldMapping created according to the provided naming
   *         conventions.
   */
  def apply(typeFieldConvention: NamingConvention, configFieldConvention: NamingConvention): ConfigFieldMapping = {
    if (typeFieldConvention == configFieldConvention) {
      apply(identity(_))
    } else {
      apply(typeFieldConvention.toTokens _ andThen configFieldConvention.fromTokens _)
    }
  }
}
