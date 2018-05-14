package pureconfig

import java.time.Period
import java.time.temporal.ChronoUnit
import java.time.temporal.ChronoUnit._

import scala.util.Try

import com.typesafe.config.impl.ConfigImplUtil
import pureconfig.error.{ CannotConvert, FailureReason }

/**
 * Utility functions for converting a `String` to a `Period`. The parser accepts the HOCON unit syntax.
 */
private[pureconfig] object PeriodUtils {

  /**
   * Convert a string to a Period while trying to maintain compatibility with Typesafe's abbreviations.
   */
  val fromString: String => Either[FailureReason, Period] = { str =>
    Try(Right(Period.parse(str))).getOrElse(typesafeConfigParsePeriod(str))
  }

  // This is a copy of the period parser in `com.typesafe.config.impl.SimpleConfig` adapted to be safer (dealing with
  // `Either` values instead of throwing exceptions). Try not to refactor this too much so as to be easy for anyone
  // to compare this code with the original.
  def typesafeConfigParsePeriod(input: String): Either[FailureReason, Period] = {
    val s = ConfigImplUtil.unicodeTrim(input)
    val originalUnitString = getUnits(s)
    var unitString = originalUnitString
    val numberString = ConfigImplUtil.unicodeTrim(s.substring(0, s.length - unitString.length))
    var units: ChronoUnit = null
    // this would be caught later anyway, but the error message
    // is more helpful if we check it here.
    if (numberString.length == 0) return Left(CannotConvert(input, "Period", "No number in period value"))
    if (unitString.length > 2 && !unitString.endsWith("s")) unitString = unitString + "s"
    // note that this is deliberately case-sensitive
    if (unitString == "" || unitString == "d" || unitString == "days") units = ChronoUnit.DAYS
    else if (unitString == "w" || unitString == "weeks") units = ChronoUnit.WEEKS
    else if (unitString == "m" || unitString == "mo" || unitString == "months") units = ChronoUnit.MONTHS
    else if (unitString == "y" || unitString == "years") units = ChronoUnit.YEARS
    else return Left(CannotConvert(input, "Period", s"Could not parse time unit '$originalUnitString' (try d, w, mo, y)"))
    try
      periodOf(numberString.toInt, units).left.map(CannotConvert(input, "Period", _))
    catch {
      case _: NumberFormatException =>
        Left(CannotConvert(input, "Period", s"Could not parse duration number '$numberString'"))
    }
  }

  private def periodOf(n: Int, unit: ChronoUnit): Either[String, Period] = unit match {
    case DAYS => Right(Period.ofDays(n))
    case WEEKS => Right(Period.ofWeeks(n))
    case MONTHS => Right(Period.ofMonths(n))
    case YEARS => Right(Period.ofYears(n))
    case _ => Left(unit + " cannot be converted to a java.time.Period") // cannot happen
  }

  private def getUnits(s: String): String =
    s.substring(s.lastIndexWhere(!_.isLetter) + 1)
}
