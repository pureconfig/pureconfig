package pureconfig.module

import _root_.cron4s.expr.CronExpr
import _root_.cron4s.Cron
import pureconfig.{ ConfigReader, ConfigWriter }
import pureconfig.error.CannotConvert

package object cron4s {

  implicit val cronExprConfigReader: ConfigReader[CronExpr] =
    ConfigReader.fromString(str =>
      Cron.parse(str).fold(
        err => Left(CannotConvert(str, "CronExpr", err.getMessage)),
        cronExpr => Right(cronExpr)))

  implicit val cronExprConfigWriter: ConfigWriter[CronExpr] =
    ConfigWriter[String].contramap(_.toString)
}
