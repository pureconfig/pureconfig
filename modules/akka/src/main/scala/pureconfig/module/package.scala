package pureconfig.module

import scala.concurrent.duration.FiniteDuration
import _root_.akka.util.Timeout
import com.typesafe.config.ConfigValue
import pureconfig.ConfigConvert
import pureconfig.error.ConfigReaderFailures

/**
 * ConfigConvert instances for Akka value classes.
 */
package object akka {
  implicit val timeoutCC: ConfigConvert[Timeout] = new ConfigConvert[Timeout] {
    def from(c: ConfigValue): Either[ConfigReaderFailures, Timeout] = ConfigConvert[FiniteDuration].from(c).right.map(new Timeout(_))
    def to(t: Timeout): ConfigValue = ConfigConvert[FiniteDuration].to(t.duration)
  }

}
