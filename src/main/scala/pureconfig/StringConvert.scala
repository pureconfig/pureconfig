/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import scala.util.Try

/**
 * Conversion between a type [[T]] and [[String]] with error handling for the conversion from [[String]]
 * to [[T]]
 */
trait StringConvert[T] {
  def from(str: String): Try[T]
  def to(t: T): String
}

object StringConvert {
  def apply[T](implicit conv: StringConvert[T]): StringConvert[T] = conv

  def fromUnsafe[T](fromF: String => T, toF: T => String): StringConvert[T] = new StringConvert[T] {
    override def from(str: String): Try[T] = Try(fromF(str))
    override def to(t: T): String = toF(t)
  }

  implicit val readBoolean = fromUnsafe[Boolean](_.toBoolean, _.toString)
  implicit val readDouble = fromUnsafe[Double](_.toDouble, _.toString)
  implicit val readFloat = fromUnsafe[Float](_.toFloat, _.toString)
  implicit val readInt = fromUnsafe[Int](_.toInt, _.toString)
  implicit val readLong = fromUnsafe[Long](_.toLong, _.toString)
  implicit val readShort = fromUnsafe[Short](_.toShort, _.toString)
  implicit val readString = fromUnsafe[String](identity, identity)
}
