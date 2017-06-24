package pureconfig.module.cats

import cats.data.NonEmptyList
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures }

package object syntax {

  implicit class ConfigConvertFailureOps(val failures: ConfigReaderFailures) extends AnyVal {

    /**
     * Converts this into a non-empty list of failures.
     *
     * @return a non-empty list of failures.
     */
    def toNonEmptyList: NonEmptyList[ConfigReaderFailure] = NonEmptyList(failures.head, failures.tail)
  }
}
