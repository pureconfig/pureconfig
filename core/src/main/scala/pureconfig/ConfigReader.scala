package pureconfig

import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.ConfigValue
import pureconfig.ConfigReader._
import pureconfig.ConvertHelpers._
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures, ConfigValueLocation }

/**
 * Trait for objects capable of reading objects of a given type from `ConfigValues`.
 *
 * @tparam A the type of objects readable by this `ConfigReader`
 */
trait ConfigReader[A] {

  /**
   * Convert the given configuration into an instance of `A` if possible.
   *
   * @param config The configuration from which load the config
   * @return either a list of failures or an object of type `A`
   */
  def from(config: ConfigValue): Either[ConfigReaderFailures, A]

  /**
   * Maps a function over the results of this reader.
   *
   * @param f the function to map over this reader
   * @tparam B the output type of the function
   * @return a `ConfigReader` returning the results of this reader mapped by `f`.
   */
  def map[B](f: A => B): ConfigReader[B] =
    fromFunction[B](from(_).right.map(f))

  /**
   * Maps a function that can possibly fail over the results of this reader.
   *
   * @param f the function to map over this reader
   * @tparam B the value read by the function in case of success
   * @return a `ConfigReader` returning the results of this reader mapped by `f`, with the resulting `Either` flattened
   *         as a success or failure.
   */
  def emap[B](f: A => Either[ConfigReaderFailures, B]): ConfigReader[B] =
    fromFunction[B] { cv: ConfigValue => from(cv).right.flatMap(f) }

  /**
   * Monadically bind a function over the results of this reader.
   *
   * @param f the function to bind over this reader
   * @tparam B the type of the objects readable by the resulting `ConfigReader`
   * @return a `ConfigReader` returning the results of this reader bound by `f`.
   */
  def flatMap[B](f: A => ConfigReader[B]): ConfigReader[B] =
    fromFunction[B] { cv: ConfigValue => from(cv).right.flatMap(f(_).from(cv)) }

  /**
   * Combines this reader with another, returning both results as a pair.
   *
   * @param reader the reader to combine with this one
   * @tparam B the type of the objects readable by the provided reader
   * @return a `ConfigReader` returning the results of both readers as a pair.
   */
  def zip[B](reader: ConfigReader[B]): ConfigReader[(A, B)] =
    for (a <- this; b <- reader) yield (a, b)

  /**
   * Combines this reader with another, returning the result of the first one that succeeds.
   *
   * @param reader the reader to combine with this one
   * @tparam AA the type of the objects readable by both readers
   * @return a `ConfigReader` returning the results of this reader if it succeeds and the results of `reader`
   *         otherwise.
   */
  def orElse[AA >: A, B <: AA](reader: => ConfigReader[B]): ConfigReader[AA] =
    fromFunction[AA] { cv: ConfigValue =>
      from(cv) match {
        case Right(a) => Right(a)
        case Left(failures) => reader.from(cv).left.map(failures ++ _)
      }
    }

  /**
   * Applies a function to configs before passing them to this reader.
   *
   * @param f the function to apply to input configs
   * @return a `ConfigReader` returning the results of this reader when the input configs are mapped using `f`.
   */
  def contramapConfig(f: ConfigValue => ConfigValue): ConfigReader[A] =
    fromFunction[A] { a => from(f(a)) }
}

/**
 * Provides methods to create [[ConfigReader]] instances.
 */
object ConfigReader extends BasicReaders with DerivedReaders {

  def apply[A](implicit reader: ConfigReader[A]): ConfigReader[A] = reader

  /**
   * Creates a `ConfigReader` from a function.
   *
   * @param fromF the function used to read configs to values
   * @tparam A the type of the objects readable by the returned reader
   * @return a `ConfigReader` for reading objects of type `A` using `fromF`.
   */
  def fromFunction[A](fromF: ConfigValue => Either[ConfigReaderFailures, A]) = new ConfigReader[A] {
    def from(config: ConfigValue) = fromF(config)
  }

  def fromString[A](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, A]): ConfigReader[A] = new ConfigReader[A] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, A] = stringToEitherConvert(fromF)(config)
  }

  def fromStringTry[A](fromF: String => Try[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString[A](tryF(fromF))
  }

  def fromStringOpt[A](fromF: String => Option[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString[A](optF(fromF))
  }

  def fromNonEmptyString[A](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString(string => location => ensureNonEmpty(ct)(string)(location).right.flatMap(s => fromF(s)(location)))
  }

  def fromNonEmptyStringTry[A](fromF: String => Try[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromNonEmptyString[A](tryF(fromF))
  }

  def fromNonEmptyStringOpt[A](fromF: String => Option[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromNonEmptyString[A](optF(fromF))
  }
}
