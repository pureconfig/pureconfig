package pureconfig.module.circe

import io.circe._
import io.circe.literal._

import pureconfig._
import pureconfig.syntax._

class CirceSuite extends BaseSuite {

  val confJson =
    json"""{ "long": 123, "double": 123.123, "alpha": "test", "arr": [1, 2, 3], "map": { "key1": "value1", "key2": "value2" } }"""
  val confString = """
    {
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
  val config = configValue(confString)

  it should "be able to read a config as circe json" in {
    config.to[Json].value shouldEqual confJson
  }

  it should "be able to write a config as circe json" in {
    ConfigWriter[Json].to(confJson) shouldEqual config
  }
}
