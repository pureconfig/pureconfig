package pureconfig.module.play

import _root_.play.api.Configuration
import org.scalatest._

class PlaySuite extends FlatSpec with Matchers with EitherValues {

  case class MyConfig(name: String)

  it should "be able to read a config from a Play Configuration using syntactic sugar" in {
    val expected = MyConfig("Susan")
    val conf = Configuration.from(Map("name" -> "Susan"))
    conf.to[MyConfig].right.value shouldEqual expected
  }
}
