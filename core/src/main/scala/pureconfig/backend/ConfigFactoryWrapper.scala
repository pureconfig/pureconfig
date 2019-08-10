package pureconfig.backend

import java.io.File
import java.net.URL
import java.nio.file.Path

import scala.util.control.NonFatal

import com.typesafe.config._
import pureconfig._
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

  def defaultReference(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.defaultReference())

  def defaultApplication(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.defaultApplication())

  def systemProperties(): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.systemProperties())

  /** @see `com.typesafe.config.ConfigFactory.parseString()` */
  def parseString(s: String): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.parseString(s))

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseFile(file: File): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.parseFile(file, strictSettings), Some(file.toPath))

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseFile(path: Path): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.parseFile(path.toFile, strictSettings), Some(path))

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseResources(resource: String): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.parseResources(resource, strictSettings)) // TODO

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseURL(url: URL): ConfigReader.Result[Config] =
    unsafeToReaderResult(ConfigFactory.parseURL(url, strictSettings)) // TODO

  /** Utility methods that parse a file and then calls `ConfigFactory.load` */
  def loadFile(path: Path): ConfigReader.Result[Config] =
    parseFile(path.toFile).right.flatMap(rawConfig => unsafeToReaderResult(ConfigFactory.load(rawConfig)))

  private def unsafeToReaderResult[A](f: => A, path: Option[Path] = None): ConfigReader.Result[A] = {
    try Right(f) catch {
      case e: ConfigException.IO if path.nonEmpty => ConfigReader.Result.fail(CannotReadFile(path.get, Option(e.getCause)))
      case e: ConfigException.Parse =>
        val msg = (if (e.origin != null)
          // Removing the error origin from the exception message since origin is stored and used separately:
          e.getMessage.stripPrefix(s"${e.origin.description}: ")
        else
          e.getMessage).stripSuffix(".")
        ConfigReader.Result.fail(CannotParse(msg, ConfigValueLocation(e.origin())))
      case e: ConfigException => ConfigReader.Result.fail(ThrowableFailure(e, ConfigValueLocation(e.origin())))
      case NonFatal(e) => ConfigReader.Result.fail(ThrowableFailure(e, None))
    }
  }
}
