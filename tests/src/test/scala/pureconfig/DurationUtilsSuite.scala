package pureconfig

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import com.typesafe.config.ConfigValueFactory
import org.scalatest.Inspectors
import pureconfig.DurationUtilsSuite._
import pureconfig.error.{CannotConvert, ConvertFailure, ExceptionThrown}

class DurationUtilsSuite extends BaseSuite {

  "Converting a Duration to a String" should "pick an appropriate unit when dealing with whole units less than the next step up" in {
    fromD(Duration(14, TimeUnit.DAYS)) shouldBe "14d"
    fromD(Duration(16, TimeUnit.HOURS)) shouldBe "16h"
    fromD(Duration(1, TimeUnit.MINUTES)) shouldBe "1m"
    fromD(Duration(33, TimeUnit.SECONDS)) shouldBe "33s"
    fromD(Duration(555, TimeUnit.MILLISECONDS)) shouldBe "555ms"
    fromD(Duration(999, TimeUnit.MICROSECONDS)) shouldBe "999us"
    fromD(Duration(222, TimeUnit.NANOSECONDS)) shouldBe "222ns"
  }
  it should "pick an appropriate unit when dealing with whole units more than, but fractional in, the next step up" in {
    fromD(Duration(1, TimeUnit.DAYS)) shouldBe "1d"
    fromD(Duration(47, TimeUnit.HOURS)) shouldBe "47h"
    fromD(Duration(123, TimeUnit.MINUTES)) shouldBe "123m"
    fromD(Duration(489, TimeUnit.SECONDS)) shouldBe "489s"
    fromD(Duration(11555, TimeUnit.MILLISECONDS)) shouldBe "11555ms"
    fromD(Duration(44999, TimeUnit.MICROSECONDS)) shouldBe "44999us"
    fromD(Duration(88222, TimeUnit.NANOSECONDS)) shouldBe "88222ns"
  }
  it should "write 0 without units" in {
    fromD(Duration(0, TimeUnit.DAYS)) shouldBe "0"
  }
  it should "handle an infinite duration" in {
    fromD(Duration.Inf) shouldBe "Inf"
  }
  it should "round trip negative infinity" in {
    val expected = Duration.MinusInf
    fromS(fromD(expected)).value shouldBe expected
  }

  "Converting a String to a Duration" should "succeed for known units" in {
    fromS("1d") shouldBe Right(Duration(1, TimeUnit.DAYS))
    fromS("47h") shouldBe Right(Duration(47, TimeUnit.HOURS))
    fromS("123m") shouldBe Right(Duration(123, TimeUnit.MINUTES))
    fromS("489s") shouldBe Right(Duration(489, TimeUnit.SECONDS))
    fromS("11555ms") shouldBe Right(Duration(11555, TimeUnit.MILLISECONDS))
    fromS("44999us") shouldBe Right(Duration(44999, TimeUnit.MICROSECONDS))
    fromS("44999µs") shouldBe Right(Duration(44999, TimeUnit.MICROSECONDS))
    fromS("88222ns") shouldBe Right(Duration(88222, TimeUnit.NANOSECONDS))
  }
  it should "succeed when loading 0 without units" in {
    val signs = Set("", "-", "+")
    val leftSpacing = Set("", " ")
    val rightSpacing = Set("", " ")
    val leftSpacingSize = Set((0 to 2): _*)
    val rightSpacingSize = Set((0 to 2): _*)
    val zeroRepeatSize = Set((1 to 2): _*)

    Inspectors.forAll(signs) { sign =>
      Inspectors.forAll(leftSpacing) { lsc =>
        Inspectors.forAll(rightSpacing) { rsc =>
          Inspectors.forAll(leftSpacingSize) { ls =>
            Inspectors.forAll(rightSpacingSize) { rs =>
              Inspectors.forAll(zeroRepeatSize) { zr =>
                fromS(lsc * ls + sign + "0" * zr + rsc * rs) shouldBe Right(Duration(0, TimeUnit.DAYS))
              }
            }
          }
        }
      }
    }
  }
  it should "report a helpful error message when failing to convert a bad duration" in {
    val badDuration = "10 lordsALeaping"
    BasicReaders.durationConfigReader
      .from(ConfigValueFactory.fromAnyRef(badDuration)) should failWithType[CannotConvert]
  }
  it should "correctly round trip when converting Duration.Undefined" in {
    fromS(fromD(Duration.Undefined)) shouldEqual Right(Duration.Undefined)
  }
  it should "correctly round trip when converting Duration.MinusInf" in {
    fromS(fromD(Duration.MinusInf)) shouldEqual Right(Duration.MinusInf)
  }
  it should "correctly round trip when converting Duration.Inf" in {
    fromS(fromD(Duration.Inf)) shouldEqual Right(Duration.Inf)
  }
  it should "convert a value larger than 2^52" in {
    fromS("8092048641075763 ns").value shouldBe Duration(8092048641075763L, NANOSECONDS)
  }
  it should "round trip a value which is greater than 2^52" in {
    val expected = Duration(781251341142500992L, NANOSECONDS)
    fromS(fromD(expected)).value shouldBe expected
  }
  it should "round trip a value < 2^52 which is > 2^52 when converted to milliseconds" in {
    val expected = Duration(781251341142501L, MICROSECONDS)
    fromS(fromD(expected)).value shouldBe expected
  }
  it should "freak when given a value larger than 2^64" in {
    dcc.from(scc.to("12345678901234567890 ns")) should failLike { case ConvertFailure(ExceptionThrown(ex), _, _) =>
      (include regex "trying to construct too large duration")(ex.getMessage)
    }
  }
  it should "parse a fractional value" in {
    val expected = 1.5.minutes
    fromS(fromD(expected)).value shouldBe expected
  }
  it should "change the units on a fractional value, failing to round trip in the config representation" in {
    val duration = dcc.from(scc.to("1.5 minutes")).value
    val rep = scc.from(dcc.to(duration)).value shouldBe "90s" // Not "1.5 minutes" as we might hope
  }
}

object DurationUtilsSuite {
  val scc = implicitly[ConfigConvert[String]]
  val dcc = implicitly[ConfigConvert[Duration]]
  val fromD = DurationUtils.fromDuration(_: Duration)
  val fromS = DurationUtils.fromString(_: String)
}
