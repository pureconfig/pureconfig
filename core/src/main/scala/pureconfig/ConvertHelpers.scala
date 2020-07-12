package pureconfig

import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

import pureconfig.error._

/**
  * Useful helpers for building `ConfigConvert` instances and dealing with results.
  */
trait ConvertHelpers {

  @deprecated("Use `ConfigReader.Result.zipWith` instead", "0.10.2")
  def combineResults[A, B, C](first: ConfigReader.Result[A], second: ConfigReader.Result[B])(
      f: (A, B) => C
  ): ConfigReader.Result[C] =
    ConfigReader.Result.zipWith(first, second)(f)

  @deprecated("Use `ConfigReader.Result.fail` instead", "0.10.2")
  def fail[A](failure: ConfigReaderFailure): ConfigReader.Result[A] = Left(ConfigReaderFailures(failure))

  private[pureconfig] def toResult[A, B](f: A => B): A => Either[FailureReason, B] =
    v => tryToEither(Try(f(v)))

  private[pureconfig] def tryToEither[A](t: Try[A]): Either[FailureReason, A] =
    t match {
      case Success(v) => Right(v)
      case Failure(e) => Left(ExceptionThrown(e))
    }

  private[pureconfig] def ensureNonEmpty[A](implicit ct: ClassTag[A]): String => Either[FailureReason, String] = {
    case "" => Left(EmptyStringFound(ct.toString))
    case x => Right(x)
  }

  def catchReadError[A](f: String => A)(implicit ct: ClassTag[A]): String => Either[FailureReason, A] = { string =>
    try Right(f(string))
    catch {
      case NonFatal(ex) => Left(CannotConvert(string, ct.toString(), ex.toString))
    }
  }

  /**
    * Convert a `String => Try` into a  `String => Option[ConfigValueLocation] => Either` such that after application
    * - `Success(t)` becomes `_ => Right(t)`
    * - `Failure(e)` becomes `location => Left(CannotConvert(value, type, e.getMessage, location)`
    */
  def tryF[A](f: String => Try[A])(implicit ct: ClassTag[A]): String => Either[FailureReason, A] = { string =>
    f(string) match {
      case Success(t) => Right(t)
      case Failure(e) => Left(CannotConvert(string, ct.runtimeClass.getName, e.getLocalizedMessage))
    }
  }

  /**
    * Convert a `String => Option` into a `String => Option[ConfigValueLocation] => Either` such that after application
    * - `Some(t)` becomes `_ => Right(t)`
    * - `None` becomes `location => Left(CannotConvert(value, type, "", location)`
    */
  def optF[A](f: String => Option[A])(implicit ct: ClassTag[A]): String => Either[FailureReason, A] = { string =>
    f(string) match {
      case Some(t) => Right(t)
      case None => Left(CannotConvert(string, ct.runtimeClass.getName, ""))
    }
  }
}

object ConvertHelpers extends ConvertHelpers
