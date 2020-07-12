package pureconfig.backend

import scala.util.control.NonFatal

import com.typesafe.config._
import pureconfig._
import pureconfig.error._

/**
  * Contains common utilities to deal with exceptions in unsafe methods.
  */
object ErrorUtil {

  def unsafeToReaderResult[A](
      f: => A,
      onIOFailure: Option[Option[Throwable] => CannotRead] = None
  ): ConfigReader.Result[A] = {
    try Right(f)
    catch {
      case e: ConfigException.IO if onIOFailure.nonEmpty =>
        ConfigReader.Result.fail(onIOFailure.get(Option(e.getCause)))

      case e: ConfigException.Parse =>
        val msg = (if (e.origin != null)
                     // Removing the error origin from the exception message since origin is stored and used separately:
                     e.getMessage.stripPrefix(s"${e.origin.description}: ")
                   else
                     e.getMessage).stripSuffix(".")
        ConfigReader.Result.fail(CannotParse(msg, Some(e.origin())))

      case e: ConfigException =>
        ConfigReader.Result.fail(ThrowableFailure(e, Some(e.origin())))

      case NonFatal(e) =>
        ConfigReader.Result.fail(ThrowableFailure(e, None))
    }
  }
}
