package pureconfig.module.cron4s

import com.typesafe.config.ConfigFactory
import _root_.cron4s.expr.CronExpr
import _root_.cron4s.Cron
import pureconfig.BaseSuite
import pureconfig.error.{ CannotConvert, ConfigReaderFailures, ConvertFailure }
import pureconfig.generic.auto._
import pureconfig.syntax._

class Cron4sSuite extends BaseSuite {

  case class Config(schedule: CronExpr)

  "reading valid cron expressons" should "parse the expression" in {
    val conf = ConfigFactory.parseString(s"""{schedule: "10-35 2,4,6 * ? * *"}""")

    conf.to[Config].right.value shouldEqual Config(Cron.unsafeParse("10-35 2,4,6 * ? * *"))
  }

  "reading invalid cron expressions" should "get a CannotConvert error" in {
    val conf = ConfigFactory.parseString(s"""{schedule: "10-65 * * * * *"}""")

    val errors = ConfigReaderFailures(
      ConvertFailure(
        CannotConvert("10-65 * * * * *", "CronExpr", "Expected '0-59' at position 0 but found '\"10-65 * * * * *\"'"),
        None,
        "schedule"))

    conf.to[Config].left.value shouldEqual errors
  }
}
