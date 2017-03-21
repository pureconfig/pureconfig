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
 * Trait for conversion between `T` and `ConfigValue`.
 */
trait ConfigConvert[T] {
  /**
   * Convert the given configuration into an instance of `T` if possible.
   *
   * @param config The configuration from which load the config
   * @return either a list of failures or an object of type `T`
   */
  def from(config: ConfigValue): Either[ConfigReaderFailures, T]

  /**
   * Converts a type `T` to a `ConfigValue`.
   *
   * @param t The instance of `T` to convert
   * @return The `ConfigValue` obtained from the `T` instance
   */
  def to(t: T): ConfigValue
}

object ConfigConvert extends BasicConverters with DerivedConverters {
  def apply[T](implicit conv: ConfigConvert[T]): ConfigConvert[T] = conv
}
