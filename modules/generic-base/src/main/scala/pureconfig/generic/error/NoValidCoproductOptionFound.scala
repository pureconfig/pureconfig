package pureconfig.generic.error

import com.typesafe.config.{ ConfigRenderOptions, ConfigValue }
import pureconfig.error.FailureReason

/**
 * A failure reason given when a valid option for a coproduct cannot be found.
 *
 * @param value the ConfigValue that was unable to be mapped to a coproduct option
 */
final case class NoValidCoproductOptionFound(value: ConfigValue) extends FailureReason {
  def description = s"No valid coproduct option found for '${value.render(ConfigRenderOptions.concise())}'."
}

