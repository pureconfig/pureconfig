package pureconfig

import java.time.Period

import scala.util.{Success, Try}

import com.typesafe.config.impl.ConfigImplUtil

import pureconfig.error.{CannotConvert, FailureReason}

/** Utility functions for converting a `String` to a `Period`. The parser accepts the HOCON unit syntax.
  */
private[pureconfig] object PeriodUtils {

  /** Convert a string to a Period while trying to maintain compatibility with Typesafe's abbreviations.
    */
  val fromString: String => Either[FailureReason, Period] = { str =>
    Try(Right(Period.parse(str))).getOrElse(typesafeConfigParsePeriod(str))
  }

  // This is a copy of the period parser in `com.typesafe.config.impl.SimpleConfig` adapted to be safer (dealing with
  // `Either` values instead of throwing exceptions). Try not to refactor this too much so as to be easy for anyone
  // to compare this code with the original.
  def typesafeConfigParsePeriod(input: String): Either[FailureReason, Period] = {
    val (rawValueStr, rawUnitStr) = splitUnits(ConfigImplUtil.unicodeTrim(input))
    val valueStr = ConfigImplUtil.unicodeTrim(rawValueStr)
    val unitStr = pluralize(rawUnitStr)

    // we use days as the default value in order to be compatible with Typesafe Config
    // (https://github.com/lightbend/config/blob/master/HOCON.md#period-format)
    Try(valueStr.toInt) match {
      case Success(n) =>
        unitStr match {
          case "" | "d" | "days" => Right(Period.ofDays(n))
          case "w" | "weeks" => Right(Period.ofWeeks(n))
          case "m" | "mo" | "months" => Right(Period.ofMonths(n))
          case "y" | "years" => Right(Period.ofYears(n))
          case _ => Left(CannotConvert(input, "Period", s"Could not parse time unit '$unitStr' (try d, w, mo, y)"))
        }
      case _ =>
        Left(CannotConvert(input, "Period", s"Could not parse duration number '$valueStr'"))
    }
  }

  private def splitUnits(s: String): (String, String) =
    s.splitAt(s.lastIndexWhere(!_.isLetter) + 1)

  private def pluralize(s: String): String =
    if (s.length > 2 && !s.endsWith("s")) s + "s" else s
}
