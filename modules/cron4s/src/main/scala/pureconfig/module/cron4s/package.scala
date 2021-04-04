package pureconfig.module

import _root_.cron4s.Cron
import _root_.cron4s.expr.CronExpr

import pureconfig.ConfigConvert
import pureconfig.error.CannotConvert

package object cron4s {

  implicit val cronExprConfigConvert: ConfigConvert[CronExpr] =
    ConfigConvert.viaNonEmptyString(
      str => Cron.parse(str).fold(err => Left(CannotConvert(str, "CronExpr", err.getMessage)), expr => Right(expr)),
      _.toString
    )

}
