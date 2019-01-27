package pureconfig.module

import _root_.cron4s.expr.CronExpr
import _root_.cron4s.Cron
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

package object cron4s {

  implicit val cronExprReader: ConfigReader[CronExpr] =
    ConfigReader.fromString(str =>
      Cron.parse(str).fold(
        err => Left(CannotConvert(str, "CronExpr", err.getMessage)),
        cronExpr => Right(cronExpr)))
}
