package pureconfig.module.circe

import com.typesafe.config.ConfigFactory
import io.circe._
import io.circe.literal._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import pureconfig._
import pureconfig.syntax._

class CirceSuite extends AnyFlatSpec with Matchers with EitherValues {

  val confJson =
    json"""{ "long": 123, "double": 123.123, "alpha": "test", "arr": [1, 2, 3], "map": { "key1": "value1", "key2": "value2" } }"""
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
  val source = ConfigSource.string(confString)

  it should "be able to read a config as circe json" in {
    source.at("json").load[Json].value shouldEqual confJson
  }

  it should "be able to write a config as circe json" in {
    ConfigWriter[Json].to(confJson) shouldEqual source.at("json").value().right.get
  }
}
