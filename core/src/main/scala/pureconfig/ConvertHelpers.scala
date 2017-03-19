package pureconfig

import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }
import scala.util.control.NonFatal

import com.typesafe.config._
import pureconfig.error._

/**
 * Useful helpers for building `ConfigConvert` instances and dealing with results.
 */
trait ConvertHelpers {

  def combineResults[A, B, C](first: Either[ConfigReaderFailures, A], second: Either[ConfigReaderFailures, B])(f: (A, B) => C): Either[ConfigReaderFailures, C] =
    (first, second) match {
      case (Right(a), Right(b)) => Right(f(a, b))
      case (Left(aFailures), Left(bFailures)) => Left(aFailures ++ bFailures)
      case (_, l: Left[_, _]) => l.asInstanceOf[Left[ConfigReaderFailures, Nothing]]
      case (l: Left[_, _], _) => l.asInstanceOf[Left[ConfigReaderFailures, Nothing]]
    }

  def fail[A](failure: ConfigReaderFailure): Either[ConfigReaderFailures, A] = Left(ConfigReaderFailures(failure))

  def failWithThrowable[A](throwable: Throwable): Option[ConfigValueLocation] => Either[ConfigReaderFailures, A] = location => fail[A](ThrowableFailure(throwable, location, None))

  private[this] def eitherToResult[T](either: Either[ConfigReaderFailure, T]): Either[ConfigReaderFailures, T] =
    either match {
      case r: Right[_, _] => r.asInstanceOf[Either[ConfigReaderFailures, T]]
      case Left(failure) => Left(ConfigReaderFailures(failure))
    }

  private[this] def tryToEither[T](t: Try[T]): Option[ConfigValueLocation] => Either[ConfigReaderFailure, T] = t match {
    case Success(v) => _ => Right(v)
    case Failure(e) => location => Left(ThrowableFailure(e, location, None))
  }

  private[this] def stringToTryConvert[T](fromF: String => Try[T]): ConfigValue => Either[ConfigReaderFailures, T] =
    stringToEitherConvert[T](string => location => tryToEither(fromF(string))(location))

  private[this] def stringToEitherConvert[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T]): ConfigValue => Either[ConfigReaderFailures, T] =
    config => {
      // Because we can't trust Typesafe Config not to throw, we wrap the
      // evaluation into a `try-catch` to prevent an unintentional exception from escaping.
      try {
        val string = config.valueType match {
          case ConfigValueType.STRING => config.unwrapped.toString
          case _ => config.render(ConfigRenderOptions.concise)
        }
        eitherToResult(fromF(string)(ConfigValueLocation(config)))
      } catch {
        case NonFatal(t) => failWithThrowable(t)(ConfigValueLocation(config))
      }
    }

  private[this] def ensureNonEmpty[T](implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, String] = {
    case "" => location => Left(EmptyStringFound(ct.toString(), location, None))
    case x => _ => Right(x)
  }

  def catchReadError[T](f: String => T)(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[CannotConvert, T] =
    string => location =>
      try Right(f(string)) catch {
        case NonFatal(ex) => Left(CannotConvert(string, ct.toString(), ex.toString, location, None))
      }

  /**
   * Convert a `String => Try` into a  `String => Option[ConfigValueLocation] => Either` such that after application
   * - `Success(t)` becomes `_ => Right(t)`
   * - `Failure(e)` becomes `location => Left(CannotConvert(value, type, e.getMessage, location)`
   */
  def tryF[T](f: String => Try[T])(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[CannotConvert, T] =
    string => location =>
      f(string) match {
        case Success(t) => Right(t)
        case Failure(e) => Left(CannotConvert(string, ct.runtimeClass.getName, e.getLocalizedMessage, location, None))
      }

  /**
   * Convert a `String => Option` into a `String => Option[ConfigValueLocation] => Either` such that after application
   * - `Some(t)` becomes `_ => Right(t)`
   * - `None` becomes `location => Left(CannotConvert(value, type, "", location)`
   */
  def optF[T](f: String => Option[T])(implicit ct: ClassTag[T]): String => Option[ConfigValueLocation] => Either[CannotConvert, T] =
    string => location =>
      f(string) match {
        case Some(t) => Right(t)
        case None => Left(CannotConvert(string, ct.runtimeClass.getName, "", location, None))
      }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromStringReader instead", since = "0.6.0")
  def fromString[T](fromF: String => Try[T]): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToTryConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  def fromStringReader[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T]): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToEitherConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  def fromStringReaderTry[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringReader[T](tryF(fromF))
  }

  def fromStringReaderOpt[T](fromF: String => Option[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringReader[T](optF(fromF))
  }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromNonEmptyStringReader instead", since = "0.6.0")
  def fromNonEmptyString[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromNonEmptyStringReader[T](fromF andThen tryToEither)
  }

  def fromNonEmptyStringReader[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringReader(string => location => ensureNonEmpty(ct)(string)(location).right.flatMap(s => fromF(s)(location)))
  }

  def fromNonEmptyStringReaderTry[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromNonEmptyStringReader[T](tryF(fromF))
  }

  def fromNonEmptyStringReaderOpt[T](fromF: String => Option[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromNonEmptyStringReader[T](optF(fromF))
  }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromStringConvert instead", since = "0.6.0")
  def stringConvert[T](fromF: String => Try[T], toF: T => String): ConfigConvert[T] =
    fromStringConvert[T](fromF andThen tryToEither, toF)

  def fromStringConvert[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToEitherConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(toF(t))
  }

  def fromStringConvertTry[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringConvert[T](tryF(fromF), toF)
  }

  def fromStringConvertOpt[T](fromF: String => Option[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringConvert[T](optF(fromF), toF)
  }

  @deprecated(message = "The usage of Try has been deprecated. Please use fromNonEmptyStringConvert instead", since = "0.6.0")
  def nonEmptyStringConvert[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] =
    fromNonEmptyStringConvert[T](fromF andThen tryToEither[T], toF)

  def fromNonEmptyStringConvert[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    fromStringConvert[T](string => location => ensureNonEmpty(ct)(string)(location).right.flatMap(s => fromF(s)(location)), toF)
  }

  def fromNonEmptyStringConvertTry[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]) = {
    fromNonEmptyStringConvert[T](tryF(fromF), toF)
  }

  def fromNonEmptyStringConvertOpt[T](fromF: String => Option[T], toF: T => String)(implicit ct: ClassTag[T]) = {
    fromNonEmptyStringConvert[T](optF(fromF), toF)
  }
}

object ConvertHelpers extends ConvertHelpers
