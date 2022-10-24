package pureconfig.module

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import _root_.fs2.{Stream, text}
import cats.effect.IO
import cats.effect.unsafe.implicits._
import cats.implicits._
import org.scalatest.EitherValues._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import pureconfig.error.ConfigReaderException
import pureconfig.module.{fs2 => testee}
import pureconfig.{ConfigReader, ConfigWriter}

class fs2Suite extends AnyFlatSpec with Matchers {

  private def delayEachLine(stream: Stream[IO, String], delay: FiniteDuration) = {
    val byLine = stream.through(text.lines)
    Stream.fixedDelay[IO](delay).zipWith(byLine)((_, line) => line).intersperse("\n")
  }

  case class SomeCaseClass(somefield: Int, anotherfield: String)
  implicit val reader: ConfigReader[SomeCaseClass] =
    ConfigReader.forProduct2("somefield", "anotherfield")(SomeCaseClass.apply)
  implicit val writer: ConfigWriter[SomeCaseClass] =
    ConfigWriter.forProduct2("somefield", "anotherfield") { case x @ SomeCaseClass(_, _) =>
      x
    }

  "streamConfig" should "load a case class from a byte stream" in {

    val someConfig = "somefield=1234\nanotherfield=some string"
    val configBytes = Stream.emit(someConfig).through(text.utf8.encode)

    val myConfig = testee.streamConfig[IO, SomeCaseClass](configBytes)

    myConfig.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "error when stream is blank" in {
    val blankStream = Stream.empty.covaryAll[IO, Byte]

    val configLoad = testee.streamConfig[IO, SomeCaseClass](blankStream)

    configLoad.attempt.unsafeRunSync().left.value shouldBe a[ConfigReaderException[_]]

  }

  it should "load a case class from a stream with delays" in {

    val someConfig = "somefield=1234\nanotherfield=some string"
    val configStream = Stream.emit(someConfig)
    val configBytes = delayEachLine(configStream, 200.milliseconds).through(text.utf8.encode)

    val myConfig = testee.streamConfig[IO, SomeCaseClass](configBytes)

    myConfig.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  "saveConfigToStream" should "produce HOCON for given input" in {
    val someConfig = SomeCaseClass(1234, "some string")
    val configStream: Stream[IO, Byte] = testee.saveConfigToStream(someConfig)
    val result = configStream.through(text.utf8.decode).compile.foldMonoid.unsafeRunSync()

    result should (include("somefield") and include("1234") and include("anotherfield") and include("some string"))
  }
}
