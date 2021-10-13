package pureconfig

import com.typesafe.config.{ConfigFactory, ConfigValue}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BaseSuite
    extends AnyFlatSpec
    with Matchers
    with EitherValues
    with ScalaCheckDrivenPropertyChecks
    with ConfigConvertChecks
    with ConfigReaderMatchers {

  // Creates a ConfigValue from the provided string representation.
  def configValue(confStr: String): ConfigValue = ConfigFactory.parseString(s"aux = $confStr").root.get("aux")

  // Creates a ConfigString from the provided string.
  def configString(confStr: String): ConfigValue = ConfigFactory.parseString(s"""aux = "$confStr"""").root.get("aux")
}
