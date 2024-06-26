package pureconfig

import scala.collection.mutable
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.ConfigValue

import pureconfig.ConvertHelpers._
import pureconfig.error.{ConfigReaderFailure, ConfigReaderFailures, FailureReason, UserValidationFailed}

/** Trait for objects capable of reading objects of a given type from `ConfigValues`.
  *
  * @tparam A
  *   the type of objects readable by this `ConfigReader`
  */
trait ConfigReader[A] {
  import pureconfig.ConfigReader._

  /** Convert the configuration given by a cursor into an instance of `A` if possible.
    *
    * @param cur
    *   The cursor from which the config should be loaded
    * @return
    *   either a list of failures or an object of type `A`
    */
  def from(cur: ConfigCursor): ConfigReader.Result[A]

  /** Convert the given configuration into an instance of `A` if possible.
    *
    * @param config
    *   The configuration from which the config should be loaded
    * @return
    *   either a list of failures or an object of type `A`
    */
  def from(config: ConfigValue): ConfigReader.Result[A] =
    from(ConfigCursor(config, Nil))

  /** Maps a function over the results of this reader.
    *
    * @param f
    *   the function to map over this reader
    * @tparam B
    *   the output type of the function
    * @return
    *   a `ConfigReader` returning the results of this reader mapped by `f`.
    */
  def map[B](f: A => B): ConfigReader[B] =
    fromCursor[B] { cur => from(cur).flatMap { v => cur.scopeFailure(toResult(f)(v)) } }

  /** Maps a function that can possibly fail over the results of this reader.
    *
    * @param f
    *   the function to map over this reader
    * @tparam B
    *   the value read by the function in case of success
    * @return
    *   a `ConfigReader` returning the results of this reader mapped by `f`, with the resulting `Either` flattened as a
    *   success or failure.
    */
  def emap[B](f: A => Either[FailureReason, B]): ConfigReader[B] =
    fromCursor[B] { cur => from(cur).flatMap { v => cur.scopeFailure(f(v)) } }

  /** Monadically bind a function over the results of this reader.
    *
    * @param f
    *   the function to bind over this reader
    * @tparam B
    *   the type of the objects readable by the resulting `ConfigReader`
    * @return
    *   a `ConfigReader` returning the results of this reader bound by `f`.
    */
  def flatMap[B](f: A => ConfigReader[B]): ConfigReader[B] =
    fromCursor[B] { cur => from(cur).flatMap(f(_).from(cur)) }

  /** Combines this reader with another, returning both results as a pair.
    *
    * @param reader
    *   the reader to combine with this one
    * @tparam B
    *   the type of the objects readable by the provided reader
    * @return
    *   a `ConfigReader` returning the results of both readers as a pair.
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

  /** Combines this reader with another, returning the result of the first one that succeeds.
    *
    * @param reader
    *   the reader to combine with this one
    * @tparam AA
    *   the type of the objects readable by both readers
    * @return
    *   a `ConfigReader` returning the results of this reader if it succeeds and the results of `reader` otherwise.
    */
  def orElse[AA >: A, B <: AA](reader: => ConfigReader[B]): ConfigReader[AA] =
    fromCursor[AA] { cur =>
      from(cur) match {
        case Right(a) => Right(a)
        case Left(failures) => reader.from(cur).left.map(failures ++ _)
      }
    }

  /** Applies a function to configs before passing them to this reader.
    *
    * @param f
    *   the function to apply to input configs
    * @return
    *   a `ConfigReader` returning the results of this reader when the input configs are mapped using `f`.
    */
  def contramapConfig(f: ConfigValue => ConfigValue): ConfigReader[A] =
    fromCursor[A] { cur => from(ConfigCursor(cur.valueOpt.map(f), cur.pathElems)) }

  /** Applies a function to config cursors before passing them to this reader.
    *
    * @param f
    *   the function to apply to input config cursors
    * @return
    *   a `ConfigReader` returning the results of this reader when the input cursors are mapped using `f`.
    */
  def contramapCursor(f: ConfigCursor => ConfigCursor): ConfigReader[A] =
    fromCursor[A] { cur => from(f(cur)) }

  /** Fails the reader if the condition does not hold for the result.
    *
    * @param pred
    *   the condition to assert
    * @param message
    *   the failed validation message
    * @return
    *   a `ConfigReader` returning the results of this reader if the condition holds or a failed reader otherwise.
    */
  def ensure(pred: A => Boolean, message: A => String): ConfigReader[A] =
    emap(a => Either.cond(pred(a), a, UserValidationFailed(message(a))))
}

/** Provides methods to create [[ConfigReader]] instances.
  */
object ConfigReader
    extends BasicReaders
    with CollectionReaders
    with ProductReaders
    with ExportedReaders
    with ReaderDerives {

  /** The type of most config PureConfig reading methods.
    *
    * @tparam A
    *   the type of the result
    */
  type Result[A] = Either[ConfigReaderFailures, A]

  /** Object containing useful constructors and utility methods for `Result`s.
    */
  object Result {

    /** Sequences a collection of `Result`s into a `Result` of a collection.
      */
    def sequence[A, CC[X] <: TraversableOnce[X]](
        rs: CC[ConfigReader.Result[A]]
    )(implicit cbf: FactoryCompat[A, CC[A]]): ConfigReader.Result[CC[A]] = {
      rs.foldLeft[ConfigReader.Result[mutable.Builder[A, CC[A]]]](Right(cbf.newBuilder())) {
        case (Right(builder), Right(a)) => Right(builder += a)
        case (Left(err), Right(_)) => Left(err)
        case (Right(_), Left(err)) => Left(err)
        case (Left(errs), Left(err)) => Left(errs ++ err)
      }.map(_.result())
    }

    /** Merges two `Result`s using a given function.
      */
    def zipWith[A, B, C](first: ConfigReader.Result[A], second: ConfigReader.Result[B])(
        f: (A, B) => C
    ): ConfigReader.Result[C] =
      (first, second) match {
        case (Right(a), Right(b)) => Right(f(a, b))
        case (Left(aFailures), Left(bFailures)) => Left(aFailures ++ bFailures)
        case (_, l: Left[_, _]) => l.asInstanceOf[Left[ConfigReaderFailures, Nothing]]
        case (l: Left[_, _], _) => l.asInstanceOf[Left[ConfigReaderFailures, Nothing]]
      }

    /** Returns a `Result` containing a single failure.
      */
    def fail[A](failure: ConfigReaderFailure): ConfigReader.Result[A] = Left(ConfigReaderFailures(failure))
  }

  def apply[A](implicit reader: ConfigReader[A]): ConfigReader[A] = reader

  /** Creates a `ConfigReader` from a function reading a `ConfigCursor`.
    *
    * @param fromF
    *   the function used to read config cursors to values
    * @tparam A
    *   the type of the objects readable by the returned reader
    * @return
    *   a `ConfigReader` for reading objects of type `A` using `fromF`.
    */
  def fromCursor[A](fromF: ConfigCursor => ConfigReader.Result[A]) =
    new ConfigReader[A] {
      def from(cur: ConfigCursor) = fromF(cur)
    }

  /** Creates a `ConfigReader` from a function.
    *
    * @param fromF
    *   the function used to read configs to values
    * @tparam A
    *   the type of the objects readable by the returned reader
    * @return
    *   a `ConfigReader` for reading objects of type `A` using `fromF`.
    */
  def fromFunction[A](fromF: ConfigValue => ConfigReader.Result[A]) =
    fromCursor(_.asConfigValue.flatMap(fromF))

  def fromString[A](fromF: String => Either[FailureReason, A]): ConfigReader[A] =
    ConfigReader.fromCursor(_.asString).emap(fromF)

  def fromStringTry[A](fromF: String => Try[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString[A](tryF(fromF))
  }

  def fromStringOpt[A](fromF: String => Option[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString[A](optF(fromF))
  }

  def fromNonEmptyString[A](fromF: String => Either[FailureReason, A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromString(string => ensureNonEmpty(ct)(string).flatMap(s => fromF(s)))
  }

  def fromNonEmptyStringTry[A](fromF: String => Try[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromNonEmptyString[A](tryF(fromF))
  }

  def fromNonEmptyStringOpt[A](fromF: String => Option[A])(implicit ct: ClassTag[A]): ConfigReader[A] = {
    fromNonEmptyString[A](optF(fromF))
  }
}
