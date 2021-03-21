package pureconfig

import com.typesafe.config.{ConfigValue, ConfigValueFactory}

/** Trait for objects capable of writing objects of a given type to `ConfigValues`.
  *
  * @tparam A the type of objects writable by this `ConfigWriter`
  */
trait ConfigWriter[A] {
  import pureconfig.ConfigWriter._

  /** Converts a type `A` to a `ConfigValue`.
    *
    * @param a The instance of `A` to convert
    * @return The `ConfigValue` obtained from the `A` instance
    */
  def to(a: A): ConfigValue

  /** Applies a function to values before passing them to this writer.
    *
    * @param f the function to apply to input values
    * @tparam B the input type of the function
    * @return a `ConfigWriter` that writes the results of this writer when the input values are mapped using `f`.
    */
  def contramap[B](f: B => A): ConfigWriter[B] =
    fromFunction[B] { a => to(f(a)) }

  /** Maps a function over the results of this writer.
    *
    * @param f the function to map over this writer
    * @return a `ConfigWriter` returning the results of this writer mapped by `f`.
    */
  def mapConfig(f: ConfigValue => ConfigValue): ConfigWriter[A] =
    fromFunction[A] { a => f(to(a)) }
}

/** Provides methods to create [[ConfigWriter]] instances.
  */
object ConfigWriter extends BasicWriters with CollectionWriters with ProductWriters with ExportedWriters {

  def apply[A](implicit writer: ConfigWriter[A]): ConfigWriter[A] = writer

  /** Creates a `ConfigWriter` from a function.
    *
    * @param toF the function used to write values to configs
    * @tparam A the type of the objects writable by the returned writer
    * @return a `ConfigWriter` for writing objects of type `A` using `toF`.
    */
  def fromFunction[A](toF: A => ConfigValue) =
    new ConfigWriter[A] {
      def to(a: A) = toF(a)
    }

  /** Returns a `ConfigWriter` for types supported by `ConfigValueFactory.fromAnyRef`. This method should be used
    * carefully, as a runtime exception is thrown if the type passed as argument is not supported.
    *
    * @tparam A the primitive type for which a `ConfigWriter` is to be created
    * @return a `ConfigWriter` for the type `A`.
    */
  def forPrimitive[A]: ConfigWriter[A] =
    new ConfigWriter[A] {
      def to(t: A) = ConfigValueFactory.fromAnyRef(t)
    }

  /** Returns a `ConfigWriter` that writes objects of a given type as strings created by `.toString`.
    *
    * @tparam A the type for which a `ConfigWriter` is to be created
    * @return a `ConfigWriter` for the type `A`.
    */
  def toDefaultString[A]: ConfigWriter[A] =
    new ConfigWriter[A] {
      def to(t: A) = ConfigValueFactory.fromAnyRef(t.toString)
    }

  /** Returns a `ConfigWriter` that writes objects of a given type as strings created by a function.
    *
    * @param toF the function converting an object of type `A` to a string
    * @tparam A the type for which a `ConfigWriter` is to be created
    * @return a `ConfigWriter` for the type `A`.
    */
  def toString[A](toF: A => String): ConfigWriter[A] =
    new ConfigWriter[A] {
      def to(t: A) = ConfigValueFactory.fromAnyRef(toF(t))
    }
}
