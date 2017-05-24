package pureconfig.module

import scala.reflect.ClassTag

import _root_.play.api.Configuration
import pureconfig.ConfigReader
import pureconfig.error.ConfigReaderFailures
import pureconfig.syntax._

/**
 * Augmented syntax for Play's Configuration class
 */
package object play {

  implicit class PimpedPlayConfig(val playConf: Configuration) extends AnyVal {
    def to[T: ConfigReader]: Either[ConfigReaderFailures, T] = playConf.underlying.root().to[T]
    def toOrThrow[T](implicit reader: ConfigReader[T], cl: ClassTag[T]): T = getResultOrThrow(playConf.underlying.root().to[T])(cl)
  }

}
