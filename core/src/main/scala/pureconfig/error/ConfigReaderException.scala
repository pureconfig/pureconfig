/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import scala.collection.mutable
import scala.reflect.ClassTag

final case class ConfigReaderException[T](failures: ConfigReaderFailures)(implicit ct: ClassTag[T]) extends RuntimeException {

  override def getMessage: String = {
    val linesBuffer = mutable.Buffer.empty[String]
    linesBuffer += s"Cannot convert configuration to a value of class ${ct.runtimeClass.getName}. Failures are:"

    val (keysNotFound, cannotConvertFound, otherFound) =
      failures.toList.foldLeft((Seq.empty[KeyNotFound], Seq.empty[CannotConvert], Seq.empty[ConfigReaderFailure])) {
        case ((keysNotFound, cannotConvertFound, otherFound), failure) =>
          failure match {
            case k: KeyNotFound => (keysNotFound :+ k, cannotConvertFound, otherFound)
            case c: CannotConvert => (keysNotFound, cannotConvertFound :+ c, otherFound)
            case o: ConfigReaderFailure => (keysNotFound, cannotConvertFound, otherFound :+ o)
          }
      }

    if (keysNotFound.nonEmpty) {
      linesBuffer += "  Keys not found:"
      for (KeyNotFound(key, location) <- keysNotFound) {
        val desc = location.fold("")(_.description)
        linesBuffer += s"    - $desc '$key'"
      }
    }

    if (cannotConvertFound.nonEmpty) {
      linesBuffer += "  Value conversion failures:"
      for (CannotConvert(value, toTyp, because, location, _) <- cannotConvertFound) {
        val desc = location.fold("")(_.description)
        linesBuffer += s"    - $desc cannot convert '$value' to type '$toTyp' " + (if (because.isEmpty) "" else s"because $because")
      }
    }

    if (otherFound.nonEmpty) {
      linesBuffer += "  Other failures:"
      for (failure <- otherFound) {
        val desc = failure.location.fold("")(_.description)
        linesBuffer += s"    - $desc $failure"
      }
    }

    linesBuffer += ""

    linesBuffer.mkString(System.lineSeparator())
  }

}
