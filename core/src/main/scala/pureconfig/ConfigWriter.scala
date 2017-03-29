package pureconfig

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }

/**
 * Trait for objects capable of writing objects of a given type to `ConfigValues`.
 *
 * @tparam T the type of objects writable by this `ConfigWriter`
 */
trait ConfigWriter[T] {

  /**
   * Converts a type `T` to a `ConfigValue`.
   *
   * @param t The instance of `T` to convert
   * @return The `ConfigValue` obtained from the `T` instance
   */
  def to(t: T): ConfigValue
}

/**
 * Provides methods to create [[ConfigWriter]] instances.
 */
object ConfigWriter extends BasicWriters with DerivedWriters {

  def apply[T](implicit writer: ConfigWriter[T]): ConfigWriter[T] = writer

  /**
   * Returns a `ConfigWriter` for types supported by `ConfigValueFactory.fromAnyRef`. This method should be used
   * carefully, as a runtime exception is thrown if the type passed as argument is not supported.
   *
   * @tparam T the primitive type for which a `ConfigWriter` is to be created
   * @return a `ConfigWriter` for the type `T`.
   */
  def forPrimitive[T]: ConfigWriter[T] = new ConfigWriter[T] {
    def to(t: T) = ConfigValueFactory.fromAnyRef(t)
  }

  /**
   * Returns a `ConfigWriter` that writes objects of a given type as strings created by `.toString`.
   *
   * @tparam T the type for which a `ConfigWriter` is to be created
   * @return a `ConfigWriter` for the type `T`.
   */
  def toDefaultString[T]: ConfigWriter[T] = new ConfigWriter[T] {
    def to(t: T) = ConfigValueFactory.fromAnyRef(t.toString)
  }

  /**
   * Returns a `ConfigWriter` that writes objects of a given type as strings created by a function.
   *
   * @param toF the function converting an object of type `T` to a string
   * @tparam T the type for which a `ConfigWriter` is to be created
   * @return a `ConfigWriter` for the type `T`.
   */
  def toString[T](toF: T => String): ConfigWriter[T] = new ConfigWriter[T] {
    def to(t: T) = ConfigValueFactory.fromAnyRef(toF(t))
  }
}
