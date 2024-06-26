package pureconfig.module.pekko

import scala.concurrent.duration._

import org.apache.pekko.actor.ActorPath
import org.apache.pekko.util.Timeout

import pureconfig.BaseSuite
import pureconfig.syntax._

class PekkoSuite extends BaseSuite {

  it should "be able to read a config with a Timeout" in {
    val expected = 5.seconds
    configValue(s"$expected").to[Timeout].value shouldEqual Timeout(expected)
  }

  it should "load a valid ActorPath" in {
    val str = "pekko://my-sys/user/service-a/worker1"
    val expected = ActorPath.fromString(str)
    configString(str).to[ActorPath].value shouldEqual expected
  }

  it should "not load invalid ActorPath" in {
    configString("this is this the path you're looking for").to[ActorPath] should be('left)
  }
}
