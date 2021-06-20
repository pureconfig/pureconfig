package pureconfig

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
    with ConfigReaderMatchers
