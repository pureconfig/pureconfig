package pureconfig.backend

import java.nio.file.Path

import scala.util.control.NonFatal

import com.typesafe.config._
import pureconfig.ConvertHelpers._
import pureconfig.error._

/**
 * A wrapper of `com.typesafe.config.ConfigFactory` whose methods return [[scala.Either]] instead
 * of throwing exceptions
 */
object ConfigFactoryWrapper {
  private[this] val strictSettings = ConfigParseOptions.defaults.setAllowMissing(false)

  /** @see `com.typesafe.config.ConfigFactory.invalidateCaches()` */
  def invalidateCaches(): Either[ConfigReaderFailures, Unit] =
    unsafeToEither(ConfigFactory.invalidateCaches())

  /** @see `com.typesafe.config.ConfigFactory.load()` */
  def load(): Either[ConfigReaderFailures, Config] =
    unsafeToEither(ConfigFactory.load())

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseFile(path: Path): Either[ConfigReaderFailures, Config] =
    unsafeToEither(ConfigFactory.parseFile(path.toFile, strictSettings), Some(path))

  /** Utility methods that parse a file and then calls `ConfigFactory.load` */
  def loadFile(path: Path): Either[ConfigReaderFailures, Config] =
    parseFile(path).right.flatMap(rawConfig => unsafeToEither(ConfigFactory.load(rawConfig)))

  private def unsafeToEither[A](f: => A, path: Option[Path] = None): Either[ConfigReaderFailures, A] = {
    try Right(f) catch {
      case e: ConfigException.IO if path.nonEmpty => fail(CannotReadFile(path.get, Option(e.getCause)))
      case e: ConfigException.Parse =>
        val msg = (if (e.origin != null)
          // Removing the error origin from the exception message since origin is stored and used separately:
          e.getMessage.stripPrefix(s"${e.origin.description}: ")
        else
          e.getMessage).stripSuffix(".")
        fail(CannotParse(msg, ConfigValueLocation(e.origin())))
      case e: ConfigException => failWithThrowable(e)(ConfigValueLocation(e.origin()))
      case NonFatal(e) => failWithThrowable(e)(None)
    }
  }
}
