package pureconfig.backend

import java.io.File
import java.net.URL
import java.nio.file.Path

import com.typesafe.config._
import pureconfig._
import pureconfig.backend.ErrorUtil._
import pureconfig.error._

/**
 * A wrapper of `com.typesafe.config.ConfigFactory` whose methods return [[scala.Either]] instead
 * of throwing exceptions
 */
object ConfigFactoryWrapper {
  private[this] val strictSettings = ConfigParseOptions.defaults.setAllowMissing(false)

  /** @see `com.typesafe.config.ConfigFactory.invalidateCaches()` */
  def invalidateCaches(): ConfigReader.Result[Unit] =
    unsafeToReaderResult(ConfigFactory.invalidateCaches())

  /** @see `com.typesafe.config.ConfigFactory.load()` */
  def load(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.load())

  /** @see `com.typesafe.config.ConfigFactory.load(Config)` */
  def load(conf: Config): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.load(conf))

  /** @see `com.typesafe.config.ConfigFactory.defaultReference()` */
  def defaultReference(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.defaultReference())

  /** @see `com.typesafe.config.ConfigFactory.defaultApplication()` */
  def defaultApplication(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.defaultApplication())

  /** @see `com.typesafe.config.ConfigFactory.defaultOverrides()` */
  def defaultOverrides(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.defaultOverrides())

  /** @see `com.typesafe.config.ConfigFactory.systemProperties()` */
  def systemProperties(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.systemProperties())

  /** @see `com.typesafe.config.ConfigFactory.parseString()` */
  def parseString(s: String): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.parseString(s))

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseFile(file: File): ConfigReader.Result[Config] =
    unsafeToReaderResult(
      ConfigFactory.parseFile(file, strictSettings),
      onIOFailure = Some(CannotReadFile(file.toPath, _)))

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseFile(path: Path): ConfigReader.Result[Config] =
    unsafeToReaderResult(
      ConfigFactory.parseFile(path.toFile, strictSettings),
      onIOFailure = Some(CannotReadFile(path, _)))

  /** @see `com.typesafe.config.ConfigFactory.parseResources()` */
  def parseResources(resource: String, classLoader: ClassLoader = null): ConfigReader.Result[Config] =
    unsafeToReaderResult(
      ConfigFactory.parseResources(resource, strictSettings.setClassLoader(classLoader)),
      onIOFailure = Some(CannotReadResource(resource, _)))

  /** @see `com.typesafe.config.ConfigFactory.parseURL()` */
  def parseURL(url: URL): ConfigReader.Result[Config] =
    unsafeToReaderResult(
      ConfigFactory.parseURL(url, strictSettings),
      onIOFailure = Some(CannotReadUrl(url, _)))

  /** Utility methods that parse a file and then calls `ConfigFactory.load` */
  def loadFile(path: Path): ConfigReader.Result[Config] =
    parseFile(path.toFile).right.flatMap(rawConfig => unsafeToReaderResult(ConfigFactory.load(rawConfig)))
}
