/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import com.typesafe.config.ConfigValue
import pureconfig.error.ConfigReaderFailures

/**
 * Trait for objects capable of reading objects of a given type from `ConfigValues`.
 *
 * @tparam T the type of objects readable by this `ConfigReader`
 */
trait ConfigReader[T] {

  /**
   * Convert the given configuration into an instance of `T` if possible.
   *
   * @param config The configuration from which load the config
   * @return either a list of failures or an object of type `T`
   */
  def from(config: ConfigValue): Either[ConfigReaderFailures, T]
}

object ConfigReader extends BasicReaders with DerivedReaders {
  def apply[T](implicit reader: ConfigReader[T]): ConfigReader[T] = reader
}

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

object ConfigWriter extends BasicWriters with DerivedWriters {
  def apply[T](implicit writer: ConfigWriter[T]): ConfigWriter[T] = writer
}

/**
 * Trait for objects capable of reading and writing objects of a given type from and to `ConfigValues`.
 */
trait ConfigConvert[T] extends ConfigReader[T] with ConfigWriter[T]

object ConfigConvert extends ConvertHelpers {
  def apply[T](implicit conv: ConfigConvert[T]): ConfigConvert[T] = conv

  implicit def fromReaderAndWriter[T](implicit reader: ConfigReader[T], writer: ConfigWriter[T]) = new ConfigConvert[T] {
    def from(config: ConfigValue) = reader.from(config)
    def to(t: T) = writer.to(t)
  }
}
