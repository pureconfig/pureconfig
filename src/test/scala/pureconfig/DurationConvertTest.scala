package pureconfig

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigValueFactory
import org.scalatest.{ FlatSpec, Matchers, TryValues }

import scala.concurrent.duration.Duration
import DurationConvert.from

import scala.util.{ Failure, Success }

class DurationConvertTest extends FlatSpec with Matchers with TryValues {
  "Converting a Duration to a String" should "pick an appropriate unit when dealing with whole units less than the next step up" in {
    from(Duration(14, TimeUnit.DAYS)) shouldBe "14d"
    from(Duration(16, TimeUnit.HOURS)) shouldBe "16h"
    from(Duration(1, TimeUnit.MINUTES)) shouldBe "1m"
    from(Duration(33, TimeUnit.SECONDS)) shouldBe "33s"
    from(Duration(555, TimeUnit.MILLISECONDS)) shouldBe "555ms"
    from(Duration(999, TimeUnit.MICROSECONDS)) shouldBe "999us"
    from(Duration(222, TimeUnit.NANOSECONDS)) shouldBe "222ns"
  }
  it should "pick an appropriate unit when dealing with whole units more than, but fractional in, the next step up" in {
    from(Duration(1, TimeUnit.DAYS)) shouldBe "1d"
    from(Duration(47, TimeUnit.HOURS)) shouldBe "47h"
    from(Duration(123, TimeUnit.MINUTES)) shouldBe "123m"
    from(Duration(489, TimeUnit.SECONDS)) shouldBe "489s"
    from(Duration(11555, TimeUnit.MILLISECONDS)) shouldBe "11555ms"
    from(Duration(44999, TimeUnit.MICROSECONDS)) shouldBe "44999us"
    from(Duration(88222, TimeUnit.NANOSECONDS)) shouldBe "88222ns"
  }
  it should "write 0 without units" in {
    from(Duration(0, TimeUnit.DAYS)) shouldBe "0"
  }

  "Converting a String to a Duration" should "succeed for known units" in {
    from("1d") shouldBe Success(Duration(1, TimeUnit.DAYS))
    from("47h") shouldBe Success(Duration(47, TimeUnit.HOURS))
    from("123m") shouldBe Success(Duration(123, TimeUnit.MINUTES))
    from("489s") shouldBe Success(Duration(489, TimeUnit.SECONDS))
    from("11555ms") shouldBe Success(Duration(11555, TimeUnit.MILLISECONDS))
    from("44999us") shouldBe Success(Duration(44999, TimeUnit.MICROSECONDS))
    from("88222ns") shouldBe Success(Duration(88222, TimeUnit.NANOSECONDS))
  }
  it should "report a helpful error message when failing to convert a bad duration" in {
    val badDuration = "10 lordsALeaping"
    val result = LocalDurationConvert.durationConfigConvert.from(ConfigValueFactory.fromAnyRef(badDuration))
    result match {
      case Success(_) => fail("Should be failure")
      case Failure(ex) =>
        val message = ex.getMessage
        println(message)
        message should include(badDuration)
    }
  }
  private object LocalDurationConvert extends LowPriorityConfigConvertImplicits
}
