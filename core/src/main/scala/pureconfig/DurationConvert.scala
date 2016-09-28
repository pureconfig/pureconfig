package pureconfig

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * Utility functions for converting a Duration to a String and vice versa.
 */
private[pureconfig] object DurationConvert {
  /**
   * Convert a string to a Duration using Typesafe Config's algorithm.
   * Typesafe Config has a really nice duration parsing algorithm; unfortunately it's private.
   * We jump through hoops to reuse that for compatibility.
   */
  def from(durationString: String): Try[Duration] = Try {
    val phonyKey = "k"
    val phonyConfigString = s"$phonyKey: $durationString"
    val javaDuration = ConfigFactory.parseString(phonyConfigString).getDuration(phonyKey)
    Duration.fromNanos(javaDuration.toNanos)
  }

  /**
   * Format a duration as a string with a suitable time unit using units TypesafeConfig understands.
   */
  def from(d: Duration): String = {
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
    microsecondInNanos -> "us"
  ).sortBy(_._1)(implicitly[Ordering[Long]].reverse)
}
