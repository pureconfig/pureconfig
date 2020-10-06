package pureconfig.generic.error

import pureconfig.error.FailureReason

/** A failure reason given when a provided coproduct option is invalid. This likely signals a bug in a CoproductHint
  * implementation, since the provided option isn't a valid one for the CoproductHint's type.
  *
  * @param option the coproduct option that is invalid
  */
final case class InvalidCoproductOption(option: String) extends FailureReason {
  def description =
    s"""|The provided option '$option' is invalid for the CoproductHint's type. There's likely a bug in the
        |CoproductHint implementation.""".stripMargin
}
