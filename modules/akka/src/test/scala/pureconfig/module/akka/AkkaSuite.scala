package pureconfig.module.akka

import scala.concurrent.duration._

import akka.actor.ActorPath
import akka.util.Timeout

import pureconfig.BaseSuite
import pureconfig.syntax._

class AkkaSuite extends BaseSuite {

  it should "be able to read a config with a Timeout" in {
    val expected = 5.seconds
    configValue(s"$expected").to[Timeout].value shouldEqual Timeout(expected)
  }

  it should "load a valid ActorPath" in {
    val str = "akka://my-sys/user/service-a/worker1"
    val expected = ActorPath.fromString(str)
    configValue(s""""$str"""").to[ActorPath].value shouldEqual expected
  }

  it should "not load invalid ActorPath" in {
    configValue(""""this is this the path you're looking for"""").to[ActorPath] should be('left)
  }
}
