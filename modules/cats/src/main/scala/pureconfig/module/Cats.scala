package pureconfig.module

import pureconfig.error.FailureReason

object Cats {

  /**
   * A failure representing an unexpected empty traversable
   *
   * @param typ the type that was attempted to be converted to from an empty string
   */
  final case class EmptyTraversableFound(typ: String) extends FailureReason {
    def description = s"Empty collection found when trying to convert to $typ."
  }
}
