/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import pureconfig.error.{ CannotConvert, ConfigReaderFailure, ConfigValueLocation }

import scala.concurrent.duration.Duration.{ Inf, MinusInf }
import scala.concurrent.duration.{ DAYS, Duration, FiniteDuration, HOURS, MICROSECONDS, MILLISECONDS, MINUTES, NANOSECONDS, SECONDS, TimeUnit }
import scala.util.Try

/**
 * Utility functions for converting a Duration to a String and vice versa.
 */
private[pureconfig] object DurationConvert {
  /**
   * Convert a string to a Duration while trying to maintain compatibility with Typesafe's abbreviations.
   */
  val fromString: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, Duration] = { string => location =>
    if (string == UndefinedDuration) Right(Duration.Undefined)
    else try {
      Right(parseDuration(addZeroUnit(justAMinute(itsGreekToMe(string)))))
    } catch {
      case ex: NumberFormatException =>
        val err = s"${ex.getMessage}. (try a number followed by any of ns, us, ms, s, m, h, d)"
        Left(CannotConvert(string, "Duration", err, location, ""))
    }
  }

  ////////////////////////////////////
  // This is a copy of Duration(str: String) that fixes the bug on precision
  //

  // "ms milli millisecond" -> List("ms", "milli", "millis", "millisecond", "milliseconds")
  private[this] def words(s: String) = (s.trim split "\\s+").toList
  private[this] def expandLabels(labels: String): List[String] = {
    val hd :: rest = words(labels)
    hd :: rest.flatMap(s => List(s, s + "s"))
  }

  private[this] val timeUnitLabels = List(
    DAYS -> "d day",
    HOURS -> "h hour",
    MINUTES -> "min minute",
    SECONDS -> "s sec second",
    MILLISECONDS -> "ms milli millisecond",
    MICROSECONDS -> "µs micro microsecond",
    NANOSECONDS -> "ns nano nanosecond")

  // Label => TimeUnit
  protected[pureconfig] val timeUnit: Map[String, TimeUnit] =
    timeUnitLabels.flatMap { case (unit, names) => expandLabels(names) map (_ -> unit) }.toMap

  private[pureconfig] def parseDuration(s: String): Duration = {
    val s1: String = s filterNot (_.isWhitespace)
    s1 match {
      case "Inf" | "PlusInf" | "+Inf" => Inf
      case "MinusInf" | "-Inf" => MinusInf
      case _ =>
        val unitName = s1.reverse.takeWhile(_.isLetter).reverse
        timeUnit get unitName match {
          case Some(unit) =>
            val valueStr = s1 dropRight unitName.length
            // Reading Long first avoids losing precision unnecessarily
            Try(Duration(java.lang.Long.parseLong(valueStr), unit)).getOrElse {
              // But if the value is a fractional number, then we have to parse it 
              // as a Double, which will lose precision and possibly change the units. 
              Duration(java.lang.Double.parseDouble(valueStr), unit)
            }
          case _ => throw new NumberFormatException("format error " + s)
        }
    }
  }

  ////////////////////////////////////

  private val zeroRegex = "\\s*[+-]?0+\\s*$".r
  private val fauxMuRegex = "([0-9])(\\s*)us(\\s*)$".r
  private val shortMinuteRegex = "([0-9])(\\s*)m(\\s*)$".r

  private val addZeroUnit = { s: String => if (zeroRegex.unapplySeq(s).isDefined) "0d" else s }

  // To maintain compatibility with Typesafe Config, replace "us" with "µs".
  private val itsGreekToMe = fauxMuRegex.replaceSomeIn(_: String, m => Some(s"${m.group(1)}${m.group(2)}µs${m.group(3)}"))

  // To maintain compatibility with Typesafe Config, replace "m" with "minutes".
  private val justAMinute = shortMinuteRegex.replaceSomeIn(_: String, m => Some(s"${m.group(1)}${m.group(2)}minutes${m.group(3)}"))

  /**
   * Format a possibily infinite duration as a string with a suitable time unit using units TypesafeConfig understands.
   * Caveat: TypesafeConfig doesn't undersand infinite durations
   */
  def fromDuration(d: Duration): String = {
    d match {
      case f: FiniteDuration => fromFiniteDuration(f)
      case Duration.Inf => "Inf"
      case Duration.MinusInf => "MinusInf"
      // We must do an `eq` instead of `==` comparison because `Undefined` is intentionally != itself.
      case d if d eq Duration.Undefined => UndefinedDuration
    }
  }

  /// We need our own constant for `Duration.Undefined` because that value's `toString` is `Duration.Undefined`
  /// which is inconsistent with the `Inf` and `Minus` `toString` provided by other special `Duration`s.
  private final val UndefinedDuration = "Undefined"

  /**
   * Format a FiniteDuration as a string with a suitable time unit using units TypesafeConfig understands.
   */
  def fromFiniteDuration(d: FiniteDuration): String = {
    d.toNanos match {
      case 0L => "0"
      case n =>
        timeUnitsToLabels.collectFirst {
          case (unitInNanos, unitLabel) if n >= unitInNanos && n % unitInNanos == 0 =>
            s"${n / unitInNanos}$unitLabel"
        }.getOrElse(s"${n}ns")
    }
  }

  private final val microsecondInNanos = 1000L
  private final val millisecondInNanos = 1000L * microsecondInNanos
  private final val secondInNanos = 1000L * millisecondInNanos
  private final val minuteInNanos = 60L * secondInNanos
  private final val hourInNanos = 60L * minuteInNanos
  private final val dayInNanos = 24L * hourInNanos

  // Must be sorted from largest unit to smallest.
  private final val timeUnitsToLabels = Vector(
    dayInNanos -> "d",
    hourInNanos -> "h",
    minuteInNanos -> "m",
    secondInNanos -> "s",
    millisecondInNanos -> "ms",
    microsecondInNanos -> "us").sortBy(_._1)(implicitly[Ordering[Long]].reverse)
}
