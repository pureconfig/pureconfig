package pureconfig.backend

import java.nio.file.Path

import com.typesafe.config.{ Config, ConfigFactory }
import pureconfig.error.{ ConfigReaderFailures, ThrowableFailure }

import scala.util.control.NonFatal

/**
 * A wrapper of [[com.typesafe.config.ConfigFactory]] whose methods return `Either` instead
 * of throwing exceptions
 */
object ConfigFactoryWrapper {

  /** @see [[ConfigFactory.invalidateCaches()]] */
  def invalidateCaches(): Either[ConfigReaderFailures, Unit] =
    unsafeToEither(ConfigFactory.invalidateCaches())

  /** @see [[ConfigFactory.load()]] */
  def load(): Either[ConfigReaderFailures, Config] =
    unsafeToEither(ConfigFactory.load())

  /** @see [[ConfigFactory.parseFile()]] */
  def parseFile(path: Path): Either[ConfigReaderFailures, Config] =
    unsafeToEither(ConfigFactory.parseFile(path.toFile))

  /** Utility methods that parse a file and then calls `ConfigFactory.load` */
  def loadFile(path: Path): Either[ConfigReaderFailures, Config] =
    parseFile(path).flatMap(rawConfig => unsafeToEither(ConfigFactory.load(rawConfig)))

  private def unsafeToEither[A](f: => A): Either[ConfigReaderFailures, A] = {
    try (Right(f)) catch {
      case NonFatal(e) =>
        Left(ConfigReaderFailures(ThrowableFailure(e, None)))
    }
  }
}
