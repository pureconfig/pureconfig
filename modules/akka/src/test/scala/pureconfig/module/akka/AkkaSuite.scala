package pureconfig.module.akka

import scala.concurrent.duration._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest._
import pureconfig.syntax._

class AkkaSuite extends FlatSpec with Matchers with EitherValues {

  case class AkkaConf(timeout: Timeout)

  it should "be able to read a config with a Timeout" in {
    val expected = 5.seconds
    val config = ConfigFactory.parseString(s"""{ timeout: $expected }""")
    config.to[AkkaConf].right.value shouldEqual AkkaConf(Timeout(expected))
  }
}
