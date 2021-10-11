package pureconfig.module.akka

import scala.concurrent.duration._

import akka.actor.ActorPath
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import pureconfig.ConfigSource
import pureconfig.syntax._

class AkkaSuite extends AnyFlatSpec with Matchers with EitherValues {

  it should "be able to read a config with a Timeout" in {
    val expected = 5.seconds
    val source = ConfigSource.string(s"""{ timeout: $expected }""")
    source.at("timeout").load[Timeout].value shouldEqual Timeout(expected)
  }

  it should "load a valid ActorPath" in {
    val str = "akka://my-sys/user/service-a/worker1"
    val expected = ActorPath.fromString(str)
    val source = ConfigSource.string(s"""{ path: "$str" }""")
    source.at("path").load[ActorPath].value shouldEqual expected
  }

  it should "not load invalid ActorPath" in {
    val source = ConfigSource.string("""{ path: "this is this the path you're looking for" }""")
    source.at("path").load[ActorPath] should be('left)
  }
}
