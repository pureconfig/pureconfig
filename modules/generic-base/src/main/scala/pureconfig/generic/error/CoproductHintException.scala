package pureconfig.generic.error

import pureconfig.error.FailureReason

/**
  * An exception to be thrown on operations inside CoproductHints.
  *
  * @param failure the reason for the exception
  */
final case class CoproductHintException(failure: FailureReason) extends RuntimeException {
  override def getMessage: String = failure.description
}
