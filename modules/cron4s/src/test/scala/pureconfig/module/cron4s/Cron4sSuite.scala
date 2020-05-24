package pureconfig.module.cron4s

import java.util.{ Map => JMap }
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
        CannotConvert("10-65 * * * * *", "CronExpr", "blank expected at position 3 but found '-'"),
        stringConfigOrigin(1),
        "schedule"))

    conf.to[Config].left.value shouldEqual errors
  }

  "writing a cron expression" should "generate a valid configuration" in {
    val exprStr = "10-35 2,4,6 * ? * *"
    val cfg = Config(Cron.unsafeParse(exprStr))

    cfg.toConfig.unwrapped().asInstanceOf[JMap[String, String]].get("schedule") shouldEqual exprStr
  }

}
