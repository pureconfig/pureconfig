package pureconfig.module

import scala.concurrent.duration.FiniteDuration

import _root_.akka.actor.ActorPath
import _root_.akka.util.Timeout
import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.viaString
import pureconfig.ConvertHelpers.catchReadError

/**
 * ConfigConvert instances for Akka value classes.
 */
package object akka {
  implicit val timeoutCC: ConfigConvert[Timeout] =
    ConfigConvert[FiniteDuration].xmap(new Timeout(_), _.duration)

  implicit val actorPathCC: ConfigConvert[ActorPath] =
    viaString[ActorPath](catchReadError(ActorPath.fromString), _.toSerializationFormat)

}
