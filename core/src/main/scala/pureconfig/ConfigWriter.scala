package pureconfig

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }

/**
 * Trait for objects capable of writing objects of a given type to `ConfigValues`.
 *
 * @tparam A the type of objects writable by this `ConfigWriter`
 */
trait ConfigWriter[A] {

  /**
   * Converts a type `A` to a `ConfigValue`.
   *
   * @param a The instance of `A` to convert
   * @return The `ConfigValue` obtained from the `A` instance
   */
  def to(a: A): ConfigValue
}

/**
 * Provides methods to create [[ConfigWriter]] instances.
 */
object ConfigWriter extends BasicWriters with DerivedWriters {

  def apply[A](implicit writer: ConfigWriter[A]): ConfigWriter[A] = writer

  /**
   * Returns a `ConfigWriter` for types supported by `ConfigValueFactory.fromAnyRef`. This method should be used
   * carefully, as a runtime exception is thrown if the type passed as argument is not supported.
   *
   * @tparam A the primitive type for which a `ConfigWriter` is to be created
   * @return a `ConfigWriter` for the type `A`.
   */
  def forPrimitive[A]: ConfigWriter[A] = new ConfigWriter[A] {
    def to(t: A) = ConfigValueFactory.fromAnyRef(t)
  }

  /**
   * Returns a `ConfigWriter` that writes objects of a given type as strings created by `.toString`.
   *
   * @tparam A the type for which a `ConfigWriter` is to be created
   * @return a `ConfigWriter` for the type `A`.
   */
  def toDefaultString[A]: ConfigWriter[A] = new ConfigWriter[A] {
    def to(t: A) = ConfigValueFactory.fromAnyRef(t.toString)
  }

  /**
   * Returns a `ConfigWriter` that writes objects of a given type as strings created by a function.
   *
   * @param toF the function converting an object of type `A` to a string
   * @tparam A the type for which a `ConfigWriter` is to be created
   * @return a `ConfigWriter` for the type `A`.
   */
  def toString[A](toF: A => String): ConfigWriter[A] = new ConfigWriter[A] {
    def to(t: A) = ConfigValueFactory.fromAnyRef(toF(t))
  }
}
