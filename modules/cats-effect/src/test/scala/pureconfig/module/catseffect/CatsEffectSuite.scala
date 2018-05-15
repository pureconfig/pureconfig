package pureconfig.module.catseffect

import java.io._

import cats.effect.IO
import pureconfig.BaseSuite
import pureconfig.error.{ ConfigReaderException, ConvertFailure }
import java.nio.file.{ Path, Paths }

import cats.data.NonEmptyList
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

  it should "fail when the expected file is not present" in {
    val load = loadConfigF[IO, SomeCaseClass]("non-existent-namespace")

    a[ConfigReaderException[SomeCaseClass]] should be thrownBy load.unsafeRunSync()
  }

  it should "run successfully when correctly formatted file is specified as path" in {
    val propertiesPath = getPath("application.properties")

    val load = loadConfigF[IO, SomeCaseClass](propertiesPath)

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "fail when a file does not specify required keys" in {
    val propertiesPath = getPath("wrong.properties")

    val load = loadConfigF[IO, SomeCaseClass](propertiesPath)

    val thrown = the[ConfigReaderException[SomeCaseClass]] thrownBy load.unsafeRunSync()
    thrown.failures.head shouldBe a[ConvertFailure]
  }

  it should "run successfully from a Typesafe Config object" in {
    val config = ConfigFactory.load("application.properties")

    val load = loadConfigF[IO, SomeCaseClass](config)

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "run successfully from a Typesafe Config object with a namespace" in {
    val config = ConfigFactory.load("namespaced.properties")

    val load = loadConfigF[IO, SomeCaseClass](config, "somecaseclass")

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
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

  "loadConfigFromFilesF" should "combine multiple config files" in {
    val paths = NonEmptyList.of(getPath("multiple/one.properties"), getPath("multiple/two.properties"))

    val load = loadConfigFromFilesF[IO, SomeCaseClass](paths)

    load.unsafeRunSync() shouldBe SomeCaseClass(1234, "some string")
  }

  it should "fail when not all fields can be read" in {
    val load = loadConfigFromFilesF[IO, SomeCaseClass](NonEmptyList.of(getPath("multiple/one.properties")))

    a[ConfigReaderException[SomeCaseClass]] should be thrownBy load.unsafeRunSync()
  }
}
