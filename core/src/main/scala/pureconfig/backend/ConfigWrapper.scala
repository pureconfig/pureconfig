package pureconfig.backend

import com.typesafe.config._
import pureconfig.ConfigReader.Result
import pureconfig.backend.ErrorUtil._

/** Provides extension methods for `com.typesafe.config.Config` that return [[scala.Either]] instead
  * of throwing exceptions.
  */
object ConfigWrapper {

  implicit class SafeConfig(val conf: Config) extends AnyVal {

    /** @see `com.typesafe.config.Config.resolve()` */
    def resolveSafe(): Result[Config] =
      unsafeToReaderResult(conf.resolve())
  }
}
