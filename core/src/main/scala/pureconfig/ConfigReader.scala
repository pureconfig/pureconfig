package pureconfig

import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.ConfigValue
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
}

/**
 * Provides methods to create [[ConfigReader]] instances.
 */
object ConfigReader extends BasicReaders with DerivedReaders {

  def apply[A](implicit reader: ConfigReader[A]): ConfigReader[A] = reader

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
