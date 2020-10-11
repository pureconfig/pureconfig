package pureconfig

import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BaseSuite
    extends AnyFlatSpec
    with ConfigConvertChecks
    with Matchers
    with ConfigReaderMatchers
    with EitherValues
    with ScalaCheckDrivenPropertyChecks
