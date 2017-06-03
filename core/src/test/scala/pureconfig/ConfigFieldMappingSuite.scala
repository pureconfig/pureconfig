package pureconfig

import org.scalatest.{ FlatSpec, Matchers }

class ConfigFieldMappingSuite extends FlatSpec with Matchers {

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

  it should "allow defining mappings with some exceptions" in {
    val mapping = ConfigFieldMapping(CamelCase, SnakeCase).withExceptions(
      "theUglyFld" -> "the_ugly_field")

    mapping("theBeautifulField") === "the_beautiful_field"
    mapping("theUglyFld") === "the_ugly_field"
  }
}
