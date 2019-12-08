package pureconfig.module.circe

import com.typesafe.config.ConfigFactory
import io.circe._
import io.circe.literal._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.syntax._

class CirceSuite extends AnyFlatSpec with Matchers with EitherValues {

  case class JsonConf(json: Json)
  val confJson = json"""{ "long": 123, "double": 123.123, "alpha": "test", "arr": [1, 2, 3], "map": { "key1": "value1", "key2": "value2" } }"""
  val confString = """
    json = {
      long = 123
      double = 123.123
      alpha = test
      arr = [1, 2, 3]
      map = {
        key1 = value1
        key2 = value2
      }
    }
  """
  val config = ConfigFactory.parseString(confString)

  it should "be able to read a config as circe json" in {
    config.to[JsonConf].right.value shouldEqual JsonConf(confJson)
  }

  it should "be able to write a config as circe json" in {
    ConfigWriter[JsonConf].to(JsonConf(confJson)) shouldEqual config.root()
  }
}
