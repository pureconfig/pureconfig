package pureconfig.module

import scala.concurrent.duration.FiniteDuration

import _root_.akka.util.Timeout
import pureconfig.ConfigConvert

/**
 * ConfigConvert instances for Akka value classes.
 */
package object akka {
  implicit val timeoutCC: ConfigConvert[Timeout] =
    ConfigConvert[FiniteDuration].xmap(new Timeout(_), _.duration)
}
