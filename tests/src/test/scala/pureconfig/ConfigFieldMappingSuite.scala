package pureconfig

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigFieldMappingSuite extends AnyFlatSpec with Matchers {

  behavior of "ConfigFieldMapping"

  it should "allow defining a mapping using a function" in {
    val mapping = ConfigFieldMapping(_.replace("Field", "ConfigKey"))
    mapping("theBeautifulField") === "theBeautifulConfigKey"
    mapping("theUglyFld") === "theUglyFld"
  }

  it should "allow defining a mapping between two naming conventions" in {
    val mapping = ConfigFieldMapping(CamelCase, SnakeCase)
    mapping("theBeautifulField") === "the_beautiful_field"
    mapping("theUglyFld") === "the_ugly_fld"
  }

  it should "allow defining mappings with some overrides" in {
    val mapping = ConfigFieldMapping(CamelCase, SnakeCase).withOverrides(
      "theUglyFld" -> "the_ugly_field")

    mapping("theBeautifulField") === "the_beautiful_field"
    mapping("theUglyFld") === "the_ugly_field"
  }
}
