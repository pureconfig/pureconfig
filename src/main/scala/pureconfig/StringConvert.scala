/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import scala.util.Try
import java.net.URL
import StringConvert.fromUnsafe

/**
 * Conversion between a type `T` and `String` with error handling for the conversion from `String`
 * to `T`
 */
trait StringConvert[T] {
  def from(str: String): Try[T]
  def to(t: T): String
}

object StringConvert extends LowPriorityStringConvertImplicits {
  def apply[T](implicit conv: StringConvert[T]): StringConvert[T] = conv

  def fromUnsafe[T](fromF: String => T, toF: T => String): StringConvert[T] = new StringConvert[T] {
    override def from(str: String): Try[T] = Try(fromF(str))
    override def to(t: T): String = toF(t)
  }
}

/**
 * Implicit [[StringConvert]] instances defined such that they can be overriden by library consumer via a locally defined implementation.
 */
trait LowPriorityStringConvertImplicits {
  implicit val readString = fromUnsafe[String](identity, identity)
  implicit val readBoolean = fromUnsafe[Boolean](_.toBoolean, _.toString)
  implicit val readDouble = fromUnsafe[Double](_.toDouble, _.toString)
  implicit val readFloat = fromUnsafe[Float](_.toFloat, _.toString)
  implicit val readInt = fromUnsafe[Int](_.toInt, _.toString)
  implicit val readLong = fromUnsafe[Long](_.toLong, _.toString)
  implicit val readShort = fromUnsafe[Short](_.toShort, _.toString)
  implicit val readURL = fromUnsafe[URL](new URL(_), _.toString)
}
