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
    configString("10-35 2,4,6 * ? * *").to[CronExpr].value shouldEqual Cron.unsafeParse("10-35 2,4,6 * ? * *")
  }

  "reading invalid cron expressions" should "get a CannotConvert error" in {
    val exprStr = "10-65 * * * * *"
    configString(exprStr).to[CronExpr].left.value shouldEqual ConfigReaderFailures(
      ConvertFailure(
        CannotConvert(exprStr, "CronExpr", "blank expected at position 3 but found '-'"),
        stringConfigOrigin(1),
        ""
      )
    )
  }

  "writing a cron expression" should "generate a valid configuration" in {
    val exprStr = "10-35 2,4,6 * ? * *"
    Cron.unsafeParse(exprStr).toConfig.unwrapped() shouldEqual exprStr
  }

}
