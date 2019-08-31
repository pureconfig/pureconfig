package pureconfig.backend

import java.io.FileNotFoundException

import com.typesafe.config.{ ConfigException, ConfigFactory }
import pureconfig.BaseSuite
import pureconfig.PathUtils._
import pureconfig.error.{ CannotParse, CannotReadFile }

class ConfigFactoryWrapperSpec extends BaseSuite {

  behavior of "ConfigFactoryWrapper.parseFile"

  it should "return a Left when a file does not exist" in {
    ConfigFactory.parseFile(nonExistingPath.toFile) shouldEqual ConfigFactory.empty
    ConfigFactoryWrapper.parseFile(nonExistingPath) should failLike {
      case CannotReadFile(`nonExistingPath`, Some(reason)) => be(a[FileNotFoundException])(reason)
    }
  }

  it should "return a Left when a file exists but cannot be parsed" in {
    val tmpPath = createTempFile("{foo:")
    intercept[ConfigException](ConfigFactory.parseFile(tmpPath.toFile))
    ConfigFactoryWrapper.parseFile(tmpPath) should failWithType[CannotParse]
  }

  behavior of "ConfigFactoryWrapper.loadFile"

  it should "return a Left when a file does not exist" in {
    ConfigFactory.load(ConfigFactory.parseFile(nonExistingPath.toFile)) shouldEqual ConfigFactory.load(ConfigFactory.empty)
    ConfigFactoryWrapper.loadFile(nonExistingPath) should failLike {
      case CannotReadFile(`nonExistingPath`, Some(reason)) => be(a[FileNotFoundException])(reason)
    }
  }

  it should "return a Left when a file exists but cannot be parsed" in {
    val tmpPath = createTempFile("{foo:")
    intercept[ConfigException](ConfigFactory.load(ConfigFactory.parseFile(tmpPath.toFile)))
    ConfigFactoryWrapper.loadFile(tmpPath) should failWithType[CannotParse]
  }

  it should "return a Left when it finds unresolved placeholders" in {
    val tmpPath = createTempFile(f"""{ foo1: "bla", foo2: $${charlie}}""")
    intercept[ConfigException](ConfigFactory.load(ConfigFactory.parseFile(tmpPath.toFile)))
    ConfigFactoryWrapper.loadFile(tmpPath) should failWithType[CannotParse]
  }
}
