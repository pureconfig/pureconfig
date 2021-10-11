package pureconfig.module.cron4s

import java.util.{Map => JMap}

import _root_.cron4s.Cron
import _root_.cron4s.expr.CronExpr
import com.typesafe.config.ConfigFactory

import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}
import pureconfig.syntax._
import pureconfig.{BaseSuite, ConfigSource}

class Cron4sSuite extends BaseSuite {

  "reading valid cron expressons" should "parse the expression" in {
    val source = ConfigSource.string(s"""{schedule: "10-35 2,4,6 * ? * *"}""")

    source.at("schedule").load[CronExpr].value shouldEqual Cron.unsafeParse("10-35 2,4,6 * ? * *")
  }

  "reading invalid cron expressions" should "get a CannotConvert error" in {
    val source = ConfigSource.string(s"""{schedule: "10-65 * * * * *"}""")

    val errors = ConfigReaderFailures(
      ConvertFailure(
        CannotConvert("10-65 * * * * *", "CronExpr", "blank expected at position 3 but found '-'"),
        stringConfigOrigin(1),
        "schedule"
      )
    )

    source.at("schedule").load[CronExpr].left.value shouldEqual errors
  }

  "writing a cron expression" should "generate a valid configuration" in {
    val exprStr = "10-35 2,4,6 * ? * *"
    val cfg = Cron.unsafeParse(exprStr)

    cfg.toConfig.unwrapped() shouldEqual exprStr
  }

}
