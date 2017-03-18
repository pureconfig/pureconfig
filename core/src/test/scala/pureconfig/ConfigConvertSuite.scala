package pureconfig

import java.net.{URI, URL}
import java.nio.file.Path
import java.time._
import java.util.UUID

import com.typesafe.config.{Config, ConfigList, ConfigObject, ConfigValue}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{EitherValues, FlatSpec, Matchers}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.ClassTag

class ConfigReaderSuite extends FlatSpec with CheckAllValues with Matchers with GeneratorDrivenPropertyChecks with EitherValues {

  behavior of "ConfigConvert"

  checkAll[Duration]
  checkAll[FiniteDuration]
  checkAll[Instant]
  checkAll[ZoneOffset]
  checkAll[ZoneId]
  checkAll[Period]
  checkAll[Year]
  checkAll[String]
  checkAll[Boolean]
  checkAll[Double]
  checkAll[Float]
  checkAll[Int]
  checkAll[Long]
  checkAll[Short]
  checkAll[URL]
  checkAll[UUID]
  checkAll[Path]
  checkAll[URI]
  checkAll[Config]
  checkAll[ConfigObject]
  checkAll[ConfigValue]
  checkAll[ConfigList]
}

trait CheckAllValues { this: FlatSpec with Matchers with GeneratorDrivenPropertyChecks with EitherValues =>

  def checkAll[T](implicit cc: ConfigConvert[T], arb: Arbitrary[T], tag: ClassTag[T]): Unit =
    it should s"read a ${tag.runtimeClass.getSimpleName}" in forAll {
      (t: T) => cc.from(cc.to(t)).right.value shouldEqual t
    }
}
