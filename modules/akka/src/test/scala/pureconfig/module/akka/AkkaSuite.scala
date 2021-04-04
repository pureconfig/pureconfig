package pureconfig.module.akka

import scala.concurrent.duration._

import akka.actor.ActorPath
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import pureconfig.generic.auto._
import pureconfig.syntax._

class AkkaSuite extends AnyFlatSpec with Matchers with EitherValues {

  case class TimeoutConf(timeout: Timeout)
  case class PathConf(path: ActorPath)

  it should "be able to read a config with a Timeout" in {
    val expected = 5.seconds
    val config = ConfigFactory.parseString(s"""{ timeout: $expected }""")
    config.to[TimeoutConf].value shouldEqual TimeoutConf(Timeout(expected))
  }

  it should "load a valid ActorPath" in {
    val str = "akka://my-sys/user/service-a/worker1"
    val expected = ActorPath.fromString(str)
    val config = ConfigFactory.parseString(s"""{ path: "$str" }""")
    config.to[PathConf].value shouldEqual PathConf(expected)
  }

  it should "not load invalid ActorPath" in {
    val config = ConfigFactory.parseString("""{ path: "this is this the path you're looking for" }""")
    config.to[PathConf] should be('left)
  }
}
