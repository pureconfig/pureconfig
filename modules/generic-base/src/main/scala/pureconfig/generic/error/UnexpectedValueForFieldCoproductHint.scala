package pureconfig.generic.error

import com.typesafe.config.{ ConfigRenderOptions, ConfigValue }
import pureconfig.error.FailureReason

/**
 * A failure reason given when an unknown value was found in the discriminating field of a config value, when using a
 * `FieldCoproductHint`.
 *
 * @param value the value found in the discriminating field of a config value
 */
final case class UnexpectedValueForFieldCoproductHint(value: ConfigValue) extends FailureReason {
  def description =
    s"Unexpected value ${value.render(ConfigRenderOptions.concise())} found. Note that the default transformation " +
      "for representing class names in config values changed from converting to lower case to converting to kebab " +
      "case in version 0.11.0 of PureConfig. See " +
      "https://pureconfig.github.io/docs/overriding-behavior-for-sealed-families.html for more details on how to use " +
      "a different transformation."
}
