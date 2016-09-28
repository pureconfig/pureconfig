package pureconfig

import org.scalatest.FreeSpec
import pureconfig.contrib.implicits._
import argonaut.ArgonautShapeless._

class PureconfigArgonautTest extends FreeSpec {
  def readConfig: Config = loadConfig[Config].get
  "Config should be read successfully" in readConfig
  "Config has good values" in {
    val config = readConfig
    assert(config.wrapper.string.s == "Hello pureconfig-argonaut")
    assert(config.wrapper.int.i == 123)
    assert(!config.wrapper.bool.b)
  }
}
