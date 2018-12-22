package pureconfig

import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.ConfigValue
import pureconfig.ConfigReader._
import pureconfig.ConvertHelpers._
import pureconfig.error.FailureReason

/**
 * Trait for objects capable of reading objects of a given type from `ConfigValues`.
 *
 * @tparam A the type of objects readable by this `ConfigReader`
 */
trait ConfigReader[A] {

  /**
   * Convert the configuration given by a cursor into an instance of `A` if possible.
   *
   * @param cur The cursor from which the config should be loaded
   * @return either a list of failures or an object of type `A`
   */
  def from(cur: ConfigCursor): ReaderResult[A]

  /**
   * Convert the given configuration into an instance of `A` if possible.
   *
   * @param config The configuration from which the config should be loaded
   * @return either a list of failures or an object of type `A`
   */
  def from(config: ConfigValue): ReaderResult[A] =
    from(ConfigCursor(config, Nil))

  /**
   * Maps a function over the results of this reader.
   *
   * @param f the function to map over this reader
   * @tparam B the output type of the function
   * @return a `ConfigReader` returning the results of this reader mapped by `f`.
   */
  def map[B](f: A => B): ConfigReader[B] =
    fromCursor[B] { cur => from(cur).right.flatMap { v => cur.scopeFailure(toResult(f)(v)) } }

  /**
   * Maps a function that can possibly fail over the results of this reader.
   *
   * @param f the function to map over this reader
   * @tparam B the value read by the function in case of success
   * @return a `ConfigReader` returning the results of this reader mapped by `f`, with the resulting `Either` flattened
   *         as a success or failure.
   */
  def emap[B](f: A => Either[FailureReason, B]): ConfigReader[B] =
    fromCursor[B] { cur => from(cur).right.flatMap { v => cur.scopeFailure(f(v)) } }

  /**
   * Monadically bind a function over the results of this reader.
   *
   * @param f the function to bind over this reader
   * @tparam B the type of the objects readable by the resulting `ConfigReader`
   * @return a `ConfigReader` returning the results of this reader bound by `f`.
   */
  def flatMap[B](f: A => ConfigReader[B]): ConfigReader[B] =
    fromCursor[B] { cur => from(cur).right.flatMap(f(_).from(cur)) }

  /**
   * Combines this reader with another, returning both results as a pair.
   *
   * @param reader the reader to combine with this one
   * @tparam B the type of the objects readable by the provided reader
   * @return a `ConfigReader` returning the results of both readers as a pair.
   */
  def zip[B](reader: ConfigReader[B]): ConfigReader[(A, B)] =
    fromCursor[(A, B)] { cur =>
      (from(cur), reader.from(cur)) match {
        case (Right(a), Right(b)) => Right((a, b))
        case (Left(fa), Right(_)) => Left(fa)
        case (Right(_), Left(fb)) => Left(fb)
        case (Left(fa), Left(fb)) => Left(fa ++ fb)
      }
    }

  /**
   * Combines this reader with another, returning the result of the first one that succeeds.
   *
   * @param reader the reader to combine with this one
   * @tparam AA the type of the objects readable by both readers
   * @return a `ConfigReader` returning the results of this reader if it succeeds and the results of `reader`
   *         otherwise.
   */
  def orElse[AA >: A, B <: AA](reader: => ConfigReader[B]): ConfigReader[AA] =
    fromCursor[AA] { cur =>
      from(cur) match {
        case Right(a) => Right(a)
        case Left(failures) => reader.from(cur).left.map(failures ++ _)
      }
    }

  /**
   * Applies a function to configs before passing them to this reader.
   *
   * @param f the function to apply to input configs
   * @return a `ConfigReader` returning the results of this reader when the input configs are mapped using `f`.
   */
  def contramapConfig(f: ConfigValue => ConfigValue): ConfigReader[A] =
    fromCursor[A] { cur => from(ConfigCursor(f(cur.value), cur.pathElems)) }

  /**
   * Applies a function to config cursors before passing them to this reader.
   *
   * @param f the function to apply to input config cursors
   * @return a `ConfigReader` returning the results of this reader when the input cursors are mapped using `f`.
   */
  def contramapCursor(f: ConfigCursor => ConfigCursor): ConfigReader[A] =
    fromCursor[A] { cur => from(f(cur)) }
}

/**
 * Provides methods to create [[ConfigReader]] instances.
 */
object ConfigReader extends BasicReaders with CollectionReaders with ProductReaders with ExportedReaders {

  def apply[A](implicit reader: Derivation[ConfigReader[A]]): ConfigReader[A] = reader.value

  /**
   * Creates a `ConfigReader` from a function reading a `ConfigCursor`.
   *
   * @param fromF the function used to read config cursors to values
   * @tparam A the type of the objects readable by the returned reader
   * @return a `ConfigReader` for reading objects of type `A` using `fromF`.
   */
  def fromCursor[A](fromF: ConfigCursor => ReaderResult[A]) = new ConfigReader[A] {
    def from(cur: ConfigCursor) = fromF(cur)
  }

  /**
   * Creates a `ConfigReader` from a function.
   *
   * @param fromF the function used to read configs to values
   * @tparam A the type of the objects readable by the returned reader
   * @return a `ConfigReader` for reading objects of type `A` using `fromF`.
   */
  def fromFunction[A](fromF: ConfigValue => ReaderResult[A]) =
    fromCursor(fromF.compose(_.value))

  def fromString[A](fromF: String => Either[FailureReason, A]): ConfigReader[A] =
    ConfigReader.fromCursor(_.asString).emap(fromF)

  def fromStringTry[A](fromF: String => Try[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString[A](tryF(fromF))
  }

  def fromStringOpt[A](fromF: String => Option[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString[A](optF(fromF))
  }

  def fromNonEmptyString[A](fromF: String => Either[FailureReason, A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString(string => ensureNonEmpty(ct)(string).right.flatMap(s => fromF(s)))
  }

  def fromNonEmptyStringTry[A](fromF: String => Try[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromNonEmptyString[A](tryF(fromF))
  }

  def fromNonEmptyStringOpt[A](fromF: String => Option[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromNonEmptyString[A](optF(fromF))
  }
}
