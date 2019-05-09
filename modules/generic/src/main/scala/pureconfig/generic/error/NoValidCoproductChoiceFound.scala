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

