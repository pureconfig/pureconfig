package pureconfig.generic.error

import pureconfig.error.FailureReason

final case class CoproductHintException(failure: FailureReason) extends RuntimeException {
  override def getMessage: String = failure.description
}
