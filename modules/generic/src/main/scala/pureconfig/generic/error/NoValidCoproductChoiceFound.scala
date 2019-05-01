package pureconfig.generic.error

import com.typesafe.config.{ ConfigRenderOptions, ConfigValue }
import pureconfig.error.FailureReason

/**
 * A failure reason given when a valid choice for a coproduct cannot be found.
 *
 * @param value the ConfigValue that was unable to be mapped to a coproduct choice
 */
final case class NoValidCoproductChoiceFound(value: ConfigValue) extends FailureReason {
  def description = s"No valid coproduct choice found for '${value.render(ConfigRenderOptions.concise())}'."
}

/**
 * A failure reason given when an unknown value was found in the discriminating field of a config value, when using a
 * `FieldCoproductHint`.
 *
 * @param value the value found in the discriminating field of a config value
 */
final case class UnexpectedValueForFieldCoproductHint(value: ConfigValue) extends FailureReason {
  def description =
    s"Unexpected value ${value.render(ConfigRenderOptions.concise())} found. You might have a misconfigured " +
      "FieldCoproductHint. Note that the default transformation of FieldCoproductHint changed from converting to " +
      "lower case to converting to kebab case in version 0.10.3."
}
