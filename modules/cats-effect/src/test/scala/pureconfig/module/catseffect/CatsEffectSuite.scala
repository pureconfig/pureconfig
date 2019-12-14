package pureconfig.module.catseffect

import java.io._

import cats.effect.IO
import pureconfig.{ BaseSuite, ConfigSource }
import pureconfig.error.{ ConfigReaderException, ConvertFailure }
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import java.nio.file.{ Path, Paths }

import com.typesafe.config.ConfigFactory

class CatsEffectSuite extends BaseSuite {

  case class SomeCaseClass(somefield: Int, anotherfield: String)

  private def getPath(classPathPath: String): Path = {
    val resource = getClass.getClassLoader.getResource(classPathPath)
    Paths.get(resource.toURI)
  }

  "loadConfigF" should "run successfully when correctly formatted file is in place" in {
    val load = loadConfigF[IO, SomeCaseClass]

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  "ConfigSource#loadF" should "fail when the expected file is not present" in {
    val load = ConfigSource.default.at("non-existent-namespace").loadF[IO, SomeCaseClass]

    a[ConfigReaderException[SomeCaseClass]] should be thrownBy load.unsafeRunSync()
  }

  it should "run successfully when correctly formatted file is specified as path" in {
    val propertiesPath = getPath("application.properties")

    val load = ConfigSource.file(propertiesPath).loadF[IO, SomeCaseClass]

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "fail when a file does not specify required keys" in {
    val propertiesPath = getPath("wrong.properties")

    val load = ConfigSource.file(propertiesPath).loadF[IO, SomeCaseClass]

    val thrown = the[ConfigReaderException[SomeCaseClass]] thrownBy load.unsafeRunSync()
    thrown.failures.head shouldBe a[ConvertFailure]
  }

  it should "run successfully from a Typesafe Config object" in {
    val config = ConfigFactory.load("application.properties")

    val load = ConfigSource.fromConfig(config).loadF[IO, SomeCaseClass]

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "run successfully from a Typesafe Config object with a namespace" in {
    val config = ConfigFactory.load("namespaced.properties")

    val load = ConfigSource.fromConfig(config).at("somecaseclass").loadF[IO, SomeCaseClass]

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "fail when ran with a Typesafe Config object that doesn't match the format" in {
    val config = ConfigFactory.load("wrong.properties")

    val load = ConfigSource.fromConfig(config).loadF[IO, SomeCaseClass]

    val thrown = the[ConfigReaderException[SomeCaseClass]] thrownBy load.unsafeRunSync()
    thrown.failures.head shouldBe a[ConvertFailure]
  }

  it should "fail if the Typesafe config object doesn't have the required field in a namespace" in {
    val config = ConfigFactory.load("namespaced-wrong.properties")

    val load = ConfigSource.fromConfig(config).at("somecaseclass").loadF[IO, SomeCaseClass]

    val thrown = the[ConfigReaderException[SomeCaseClass]] thrownBy load.unsafeRunSync()
    thrown.failures.head shouldBe a[ConvertFailure]
  }

  "saveConfigToStreamF" should "delay writing to stream until run" in {
    val pipeInput = new PipedInputStream()
    val outputStream = new BufferedOutputStream(new PipedOutputStream(pipeInput))

    val someConfig = SomeCaseClass(1234, "some string")
    val save = saveConfigToStreamF[IO, SomeCaseClass](someConfig, outputStream)

    pipeInput.available shouldBe 0

    save.unsafeRunSync()

    pipeInput.available should be > 0
  }
}
