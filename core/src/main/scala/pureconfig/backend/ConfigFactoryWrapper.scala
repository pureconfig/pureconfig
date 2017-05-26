package pureconfig.backend

import java.nio.file.Path

import scala.util.control.NonFatal

import com.typesafe.config.{ Config, ConfigException, ConfigFactory }
import pureconfig.error._

/**
 * A wrapper of `com.typesafe.config.ConfigFactory` whose methods return [[scala.Either]] instead
 * of throwing exceptions
 */
object ConfigFactoryWrapper {

  /** @see `com.typesafe.config.ConfigFactory.invalidateCaches()` */
  def invalidateCaches(): Either[ConfigReaderFailures, Unit] =
    unsafeToEither(ConfigFactory.invalidateCaches())

  /** @see `com.typesafe.config.ConfigFactory.load()` */
  def load(): Either[ConfigReaderFailures, Config] =
    unsafeToEither(ConfigFactory.load())

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseFile(path: Path): Either[ConfigReaderFailures, Config] = {
    if (!path.toFile.isFile || !path.toFile.canRead) Left(ConfigReaderFailures(CannotReadFile(path)))
    else unsafeToEither(ConfigFactory.parseFile(path.toFile))
  }

  /** Utility methods that parse a file and then calls `ConfigFactory.load` */
  def loadFile(path: Path): Either[ConfigReaderFailures, Config] =
    parseFile(path).right.flatMap(rawConfig => unsafeToEither(ConfigFactory.load(rawConfig)))

  private def unsafeToEither[A](f: => A): Either[ConfigReaderFailures, A] = {
    try Right(f) catch {
      case e: ConfigException.Parse =>
        Left(ConfigReaderFailures(CannotParse(e.getLocalizedMessage, ConfigValueLocation(e.origin()))))
      case e: ConfigException =>
        Left(ConfigReaderFailures(ThrowableFailure(e, ConfigValueLocation(e.origin()), "")))
      case NonFatal(e) =>
        Left(ConfigReaderFailures(ThrowableFailure(e, None, "")))
    }
  }
}
