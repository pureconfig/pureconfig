package pureconfig.generic.error

import pureconfig.error.FailureReason

/**
 * A failure reason given when a given coproduct option is missing.
 *
 * @param option the coproduct option that is missing
 */
final case class MissingCoproductChoice(option: String) extends FailureReason {
  def description = s"Missing coproduct choice '$option''"
}
