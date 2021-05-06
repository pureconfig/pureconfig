package pureconfig.generic.error

import com.typesafe.config.{ConfigRenderOptions, ConfigValue}

import pureconfig.error.{ConfigReaderFailures, FailureReason}

/** A failure reason given when a valid option for a coproduct cannot be found.
  *
  * @param value the ConfigValue that was unable to be mapped to a coproduct option
  * @param optionFailures the failures produced when attempting to read coproduct options
  */
final case class NoValidCoproductOptionFound(value: ConfigValue, optionFailures: Seq[(String, ConfigReaderFailures)])
    extends FailureReason {
  def description = {
    val baseDescription = s"No valid coproduct option found for '${value.render(ConfigRenderOptions.concise())}'."
    baseDescription + (if (optionFailures.isEmpty) ""
                       else {
                         "\n" + optionFailures
                           .map { case (optionName, failures) =>
                             s"Can't use coproduct option '$optionName':\n" + failures.prettyPrint(1)
                           }
                           .mkString("\n")
                       })
  }
}
