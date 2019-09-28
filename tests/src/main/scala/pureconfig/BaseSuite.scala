package pureconfig

import org.scalatest.{ EitherValues, FlatSpec, Matchers }
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BaseSuite
  extends FlatSpec
  with ConfigConvertChecks
  with Matchers
  with ConfigReaderMatchers
  with EitherValues
  with ScalaCheckDrivenPropertyChecks
