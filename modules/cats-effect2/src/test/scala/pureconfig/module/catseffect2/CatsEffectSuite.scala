package pureconfig.module.catseffect2

import java.io.{BufferedOutputStream, PipedInputStream, PipedOutputStream}
import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext

import cats.effect.{Blocker, ContextShift, IO}
import com.typesafe.config.ConfigFactory

import pureconfig.error.{ConfigReaderException, ConvertFailure}
import pureconfig.module.catseffect2.syntax._
import pureconfig.{BaseSuite, ConfigReader, ConfigSource, ConfigWriter}

final class CatsEffectSuite extends BaseSuite {

  case class SomeCaseClass(somefield: Int, anotherfield: String)

  implicit val reader: ConfigReader[SomeCaseClass] =
    ConfigReader.forProduct2("somefield", "anotherfield")(SomeCaseClass.apply)
  implicit val writer: ConfigWriter[SomeCaseClass] =
    ConfigWriter.forProduct2("somefield", "anotherfield") { case SomeCaseClass(s, a) => (s, a) }

  private val blocker: Blocker = Blocker.liftExecutorService(Executors.newCachedThreadPool())

  private implicit val ioCS: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private def getPath(classPathPath: String): Path = {
    val resource = getClass.getClassLoader.getResource(classPathPath)
    Paths.get(resource.toURI)
  }

  "loadConfigF" should "run successfully when correctly formatted file is in place" in {
    val load = loadConfigF[IO, SomeCaseClass](blocker)

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  "ConfigSource#loadF" should "fail when the expected file is not present" in {
    val load = ConfigSource.default.at("non-existent-namespace").loadF[IO, SomeCaseClass](blocker)

    a[ConfigReaderException[SomeCaseClass]] should be thrownBy load.unsafeRunSync()
  }

  it should "run successfully when correctly formatted file is specified as path" in {
    val propertiesPath = getPath("application.properties")

    val load = ConfigSource.file(propertiesPath).loadF[IO, SomeCaseClass](blocker)

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "fail when a file does not specify required keys" in {
    val propertiesPath = getPath("wrong.properties")

    val load = ConfigSource.file(propertiesPath).loadF[IO, SomeCaseClass](blocker)

    val thrown = the[ConfigReaderException[SomeCaseClass]] thrownBy load.unsafeRunSync()
    thrown.failures.head shouldBe a[ConvertFailure]
  }

  it should "run successfully from a Typesafe Config object" in {
    val config = ConfigFactory.load("application.properties")

    val load = ConfigSource.fromConfig(config).loadF[IO, SomeCaseClass](blocker)

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "run successfully from a Typesafe Config object with a namespace" in {
    val config = ConfigFactory.load("namespaced.properties")

    val load = ConfigSource.fromConfig(config).at("somecaseclass").loadF[IO, SomeCaseClass](blocker)

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "fail when ran with a Typesafe Config object that doesn't match the format" in {
    val config = ConfigFactory.load("wrong.properties")

    val load = ConfigSource.fromConfig(config).loadF[IO, SomeCaseClass](blocker)

    val thrown = the[ConfigReaderException[SomeCaseClass]] thrownBy load.unsafeRunSync()
    thrown.failures.head shouldBe a[ConvertFailure]
  }

  it should "fail if the Typesafe config object doesn't have the required field in a namespace" in {
    val config = ConfigFactory.load("namespaced-wrong.properties")

    val load = ConfigSource.fromConfig(config).at("somecaseclass").loadF[IO, SomeCaseClass](blocker)

    val thrown = the[ConfigReaderException[SomeCaseClass]] thrownBy load.unsafeRunSync()
    thrown.failures.head shouldBe a[ConvertFailure]
  }

  "saveConfigToStreamF" should "delay writing to stream until run" in {
    val pipeInput = new PipedInputStream()
    val outputStream = new BufferedOutputStream(new PipedOutputStream(pipeInput))

    val someConfig = SomeCaseClass(1234, "some string")
    val save = blockingSaveConfigToStreamF[IO, SomeCaseClass](someConfig, outputStream, blocker)

    pipeInput.available shouldBe 0

    save.unsafeRunSync()

    pipeInput.available should be > 0
  }
}
