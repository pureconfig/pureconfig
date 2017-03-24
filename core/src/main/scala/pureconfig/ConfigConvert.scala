/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.{ ConfigValue, ConfigValueFactory }
import pureconfig.error.{ ConfigReaderFailure, ConfigReaderFailures, ConfigValueLocation }

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

  def viaString[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToEitherConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(toF(t))
  }

  def viaStringTry[T: ClassTag](fromF: String => Try[T], toF: T => String): ConfigConvert[T] = {
    viaString[T](tryF(fromF), toF)
  }

  def viaStringOpt[T: ClassTag](fromF: String => Option[T], toF: T => String): ConfigConvert[T] = {
    viaString[T](optF(fromF), toF)
  }

  def viaNonEmptyString[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    viaString[T](string => location => ensureNonEmpty(ct)(string)(location).right.flatMap(s => fromF(s)(location)), toF)
  }

  def viaNonEmptyStringTry[T: ClassTag](fromF: String => Try[T], toF: T => String): ConfigConvert[T] = {
    viaNonEmptyString[T](tryF(fromF), toF)
  }

  def viaNonEmptyStringOpt[T: ClassTag](fromF: String => Option[T], toF: T => String): ConfigConvert[T] = {
    viaNonEmptyString[T](optF(fromF), toF)
  }

  @deprecated(message = "The usage of Try has been deprecated. Please use viaString instead", since = "0.6.0")
  def stringConvert[T](fromF: String => Try[T], toF: T => String): ConfigConvert[T] =
    viaString[T](fromF andThen tryToEither, toF)

  @deprecated(message = "Please use viaString instead", since = "0.7.0")
  def fromStringConvert[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String): ConfigConvert[T] =
    viaString(fromF, toF)

  @deprecated(message = "Please use viaStringTry instead", since = "0.7.0")
  def fromStringConvertTry[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] =
    viaStringTry(fromF, toF)

  @deprecated(message = "Please use viaStringOpt instead", since = "0.7.0")
  def fromStringConvertOpt[T](fromF: String => Option[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] =
    viaStringOpt(fromF, toF)

  @deprecated(message = "The usage of Try has been deprecated. Please use viaNonEmptyString instead", since = "0.6.0")
  def nonEmptyStringConvert[T](fromF: String => Try[T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] =
    viaNonEmptyString[T](fromF andThen tryToEither[T], toF)

  @deprecated(message = "Please use viaNonEmptyString instead", since = "0.7.0")
  def fromNonEmptyStringConvert[T: ClassTag](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T], toF: T => String): ConfigConvert[T] =
    viaNonEmptyString(fromF, toF)

  @deprecated(message = "Please use viaNonEmptyStringTry instead", since = "0.7.0")
  def fromNonEmptyStringConvertTry[T: ClassTag](fromF: String => Try[T], toF: T => String): ConfigConvert[T] =
    viaNonEmptyStringTry(fromF, toF)

  @deprecated(message = "Please use viaNonEmptyStringOpt instead", since = "0.7.0")
  def fromNonEmptyStringConvertOpt[T: ClassTag](fromF: String => Option[T], toF: T => String): ConfigConvert[T] =
    viaNonEmptyStringOpt(fromF, toF)

  @deprecated(message = "The usage of Try has been deprecated. Please use fromStringReader instead", since = "0.6.0")
  def fromString[T](fromF: String => Try[T]): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, T] = stringToTryConvert(fromF)(config)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(t)
  }

  @deprecated(message = "Please use ConfigReader.fromString instead", since = "0.7.0")
  def fromStringReader[T](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T]): ConfigReader[T] =
    ConfigReader.fromString(fromF)

  @deprecated(message = "Please use ConfigReader.fromStringTry instead", since = "0.7.0")
  def fromStringReaderTry[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigReader[T] =
    ConfigReader.fromStringTry(fromF)

  @deprecated(message = "Please use ConfigReader.fromStringOpt instead", since = "0.7.0")
  def fromStringReaderOpt[T](fromF: String => Option[T])(implicit ct: ClassTag[T]): ConfigReader[T] =
    ConfigReader.fromStringOpt(fromF)

  @deprecated(message = "The usage of Try has been deprecated. Please use fromNonEmptyStringReader instead", since = "0.6.0")
  def fromNonEmptyString[T](fromF: String => Try[T])(implicit ct: ClassTag[T]): ConfigConvert[T] = new ConfigConvert[T] {
    def from(config: ConfigValue) = ConfigReader.fromNonEmptyString[T](fromF andThen tryToEither).from(config)
    def to(t: T) = ConfigValueFactory.fromAnyRef(t)
  }

  @deprecated(message = "Please use ConfigReader.fromNonEmptyString instead", since = "0.7.0")
  def fromNonEmptyStringReader[T: ClassTag](fromF: String => Option[ConfigValueLocation] => Either[ConfigReaderFailure, T]): ConfigReader[T] =
    ConfigReader.fromNonEmptyString(fromF)

  @deprecated(message = "Please use ConfigReader.fromNonEmptyStringTry instead", since = "0.7.0")
  def fromNonEmptyStringReaderTry[T: ClassTag](fromF: String => Try[T]): ConfigReader[T] =
    ConfigReader.fromNonEmptyStringTry(fromF)

  @deprecated(message = "Please use ConfigReader.fromNonEmptyStringOpt instead", since = "0.7.0")
  def fromNonEmptyStringReaderOpt[T: ClassTag](fromF: String => Option[T]): ConfigReader[T] =
    ConfigReader.fromNonEmptyStringOpt(fromF)
}
