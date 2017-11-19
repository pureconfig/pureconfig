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
import pureconfig.ConvertHelpers._
import pureconfig.error.{ ConfigReaderFailures, FailureReason }

/**
 * Trait for objects capable of reading and writing objects of a given type from and to `ConfigValues`.
 */
trait ConfigConvert[A] extends ConfigReader[A] with ConfigWriter[A] { outer =>

  /**
   * Transforms the values read and written by this `ConfigConvert` using two functions.
   *
   * @param f the function applied to values after they are read
   * @param g the function applied to values before they are written
   * @tparam B the type of the returned `ConfigConvert`
   * @return a `ConfigConvert` that reads and writes values of type `B` by applying `f` and `g` on read and write,
   *         respectively.
   */
  def xmap[B](f: A => B, g: B => A): ConfigConvert[B] = new ConfigConvert[B] {
    def from(cur: ConfigCursor) = outer.from(cur).right.flatMap { v => cur.scopeFailure(toResult(f)(v)) }
    def to(a: B) = outer.to(g(a))
  }
}

/**
 * Provides methods to create [[ConfigConvert]] instances.
 */
object ConfigConvert extends ConvertHelpers {

  def apply[T](implicit conv: ConfigConvert[T]): ConfigConvert[T] = conv

  implicit def fromReaderAndWriter[T](
    implicit
    reader: Derivation[ConfigReader[T]],
    writer: Derivation[ConfigWriter[T]]) = new ConfigConvert[T] {

    def from(cur: ConfigCursor) = reader.value.from(cur)
    def to(t: T) = writer.value.to(t)
  }

  def viaString[T](fromF: String => Either[FailureReason, T], toF: T => String): ConfigConvert[T] = new ConfigConvert[T] {
    override def from(cur: ConfigCursor): Either[ConfigReaderFailures, T] = stringToEitherConvert(fromF)(cur)
    override def to(t: T): ConfigValue = ConfigValueFactory.fromAnyRef(toF(t))
  }

  def viaStringTry[T: ClassTag](fromF: String => Try[T], toF: T => String): ConfigConvert[T] = {
    viaString[T](tryF(fromF), toF)
  }

  def viaStringOpt[T: ClassTag](fromF: String => Option[T], toF: T => String): ConfigConvert[T] = {
    viaString[T](optF(fromF), toF)
  }

  def viaNonEmptyString[T](fromF: String => Either[FailureReason, T], toF: T => String)(implicit ct: ClassTag[T]): ConfigConvert[T] = {
    viaString[T](string => ensureNonEmpty(ct)(string).right.flatMap(s => fromF(s)), toF)
  }

  def viaNonEmptyStringTry[T: ClassTag](fromF: String => Try[T], toF: T => String): ConfigConvert[T] = {
    viaNonEmptyString[T](tryF(fromF), toF)
  }

  def viaNonEmptyStringOpt[T: ClassTag](fromF: String => Option[T], toF: T => String): ConfigConvert[T] = {
    viaNonEmptyString[T](optF(fromF), toF)
  }
}
