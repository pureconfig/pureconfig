/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import scala.reflect.ClassTag
import scala.util.Try

import pureconfig.ConvertHelpers._
import pureconfig.error.FailureReason

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

  def apply[A](implicit conv: Derivation[ConfigConvert[A]]): ConfigConvert[A] = conv.value

  implicit def fromReaderAndWriter[A](
    implicit
    reader: Derivation[ConfigReader[A]],
    writer: Derivation[ConfigWriter[A]]) = new ConfigConvert[A] {

    def from(cur: ConfigCursor) = reader.value.from(cur)
    def to(t: A) = writer.value.to(t)
  }

  def viaString[A](fromF: String => Either[FailureReason, A], toF: A => String): ConfigConvert[A] =
    fromReaderAndWriter(
      Derivation.Successful(ConfigReader.fromString(fromF)),
      Derivation.Successful(ConfigWriter.toString(toF)))

  def viaStringTry[A: ClassTag](fromF: String => Try[A], toF: A => String): ConfigConvert[A] = {
    viaString[A](tryF(fromF), toF)
  }

  def viaStringOpt[A: ClassTag](fromF: String => Option[A], toF: A => String): ConfigConvert[A] = {
    viaString[A](optF(fromF), toF)
  }

  def viaNonEmptyString[A](fromF: String => Either[FailureReason, A], toF: A => String)(implicit ct: ClassTag[A]): ConfigConvert[A] = {
    viaString[A](string => ensureNonEmpty(ct)(string).right.flatMap(s => fromF(s)), toF)
  }

  def viaNonEmptyStringTry[A: ClassTag](fromF: String => Try[A], toF: A => String): ConfigConvert[A] = {
    viaNonEmptyString[A](tryF(fromF), toF)
  }

  def viaNonEmptyStringOpt[A: ClassTag](fromF: String => Option[A], toF: A => String): ConfigConvert[A] = {
    viaNonEmptyString[A](optF(fromF), toF)
  }
}
