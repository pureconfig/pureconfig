/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig

import pureconfig.ConfigConvert.catchReadError
import pureconfig.error.ConfigReaderFailure

import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.reflect.ClassTag

/**
 * Utility functions for converting a Duration to a String and vice versa.
 */
private[pureconfig] object DurationConvert {
  /**
   * Convert a string to a Duration while trying to maintain compatibility with Typesafe's abbreviations.
   */
  def fromString[D](durationString: String, ct: ClassTag[D]): Either[ConfigReaderFailure, Duration] = {
    catchReadError(durationString, string => Duration(addZeroUnit(justAMinute(itsGreekToMe(string)))))
  }

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
      case i: Duration.Infinite if i == Duration.MinusInf => "MinusInf"
      case i: Duration.Infinite => "Inf"
      case f: FiniteDuration => fromFiniteDuration(f)
    }
  }

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
