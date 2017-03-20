package pureconfig.backend

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Path }

import com.typesafe.config.{ ConfigException, ConfigFactory }
import org.scalatest.{ FlatSpec, Matchers }

class ConfigFactoryWrapperSpec extends FlatSpec with Matchers {

  def createTempConfPath(prefix: String, content: String): Path = {
    val tmpPath = Files.createTempFile(prefix, ".conf")
    tmpPath.toFile.deleteOnExit()
    val buffer = Files.newBufferedWriter(tmpPath, StandardCharsets.UTF_8)
    buffer.write(content)
    buffer.close()
    tmpPath
  }

  behavior of "ConfigFactoryWrapper.parseFile"

  it should "not throw exception but return Left on error" in {
    val tmpPath = createTempConfPath("parseFile", "{foo:")
    intercept[ConfigException](ConfigFactory.parseFile(tmpPath.toFile))
    ConfigFactoryWrapper.parseFile(tmpPath) shouldBe a[Left[_, _]]
  }

  behavior of "ConfigFactoryWrapper.loadFile"

  it should "not throw exception but return Left on error" in {
    val tmpPath = createTempConfPath("parseFile", "{foo:")
    intercept[ConfigException](ConfigFactory.load(ConfigFactory.parseFile(tmpPath.toFile)))
    ConfigFactoryWrapper.parseFile(tmpPath) shouldBe a[Left[_, _]]
  }

  it should "not throw exception but return Left when it finds unresolved placeholders" in {
    val tmpPath = createTempConfPath("parseFileTest", f"""{ foo1: "bla", foo2: $${charlie}}""")
    intercept[ConfigException](ConfigFactory.load(ConfigFactory.parseFile(tmpPath.toFile)))
    ConfigFactoryWrapper.loadFile(tmpPath) shouldBe a[Left[_, _]]
  }

}
