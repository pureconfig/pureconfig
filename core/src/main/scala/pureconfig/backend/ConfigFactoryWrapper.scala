package pureconfig.backend

import java.nio.file.Path

import scala.util.control.NonFatal

import com.typesafe.config._
import pureconfig.ReaderResult
import pureconfig.error._

/**
 * A wrapper of `com.typesafe.config.ConfigFactory` whose methods return [[scala.Either]] instead
 * of throwing exceptions
 */
object ConfigFactoryWrapper {
  private[this] val strictSettings = ConfigParseOptions.defaults.setAllowMissing(false)

  /** @see `com.typesafe.config.ConfigFactory.invalidateCaches()` */
  def invalidateCaches(): ReaderResult[Unit] =
    unsafeToEither(ConfigFactory.invalidateCaches())

  /** @see `com.typesafe.config.ConfigFactory.load()` */
  def load(): ReaderResult[Config] =
    unsafeToEither(ConfigFactory.load())

  /** @see `com.typesafe.config.ConfigFactory.parseString()` */
  def parseString(s: String): ReaderResult[Config] =
    unsafeToEither(ConfigFactory.parseString(s))

  /** @see `com.typesafe.config.ConfigFactory.parseFile()` */
  def parseFile(path: Path): ReaderResult[Config] =
    unsafeToEither(ConfigFactory.parseFile(path.toFile, strictSettings), Some(path))

  /** Utility methods that parse a file and then calls `ConfigFactory.load` */
  def loadFile(path: Path): ReaderResult[Config] =
    parseFile(path).right.flatMap(rawConfig => unsafeToEither(ConfigFactory.load(rawConfig)))

  private def unsafeToEither[A](f: => A, path: Option[Path] = None): ReaderResult[A] = {
    try Right(f) catch {
      case e: ConfigException.IO if path.nonEmpty => ReaderResult.fail(CannotReadFile(path.get, Option(e.getCause)))
      case e: ConfigException.Parse =>
        val msg = (if (e.origin != null)
          // Removing the error origin from the exception message since origin is stored and used separately:
          e.getMessage.stripPrefix(s"${e.origin.description}: ")
        else
          e.getMessage).stripSuffix(".")
        ReaderResult.fail(CannotParse(msg, ConfigValueLocation(e.origin())))
      case e: ConfigException => ReaderResult.fail(ThrowableFailure(e, ConfigValueLocation(e.origin())))
      case NonFatal(e) => ReaderResult.fail(ThrowableFailure(e, None))
    }
  }
}
