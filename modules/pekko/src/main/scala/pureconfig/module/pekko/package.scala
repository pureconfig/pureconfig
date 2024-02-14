package pureconfig.module

import scala.concurrent.duration.FiniteDuration

import org.apache.pekko.actor.ActorPath
import org.apache.pekko.util.Timeout

import pureconfig.ConfigConvert
import pureconfig.ConfigConvert.viaString
import pureconfig.ConvertHelpers.catchReadError

/** ConfigConvert instances for Pekko value classes.
  */
package object pekko {
  implicit val timeoutCC: ConfigConvert[Timeout] =
    ConfigConvert[FiniteDuration].xmap(new Timeout(_), _.duration)

  implicit val actorPathCC: ConfigConvert[ActorPath] =
    viaString[ActorPath](catchReadError(ActorPath.fromString), _.toSerializationFormat)
}
