/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package pureconfig.error

import scala.collection.mutable
import scala.reflect.ClassTag

final case class ConfigReaderException[A](failures: ConfigReaderFailures)(implicit ct: ClassTag[A])
    extends RuntimeException {

  override def getMessage: String = {
    val linesBuffer = mutable.Buffer.empty[String]
    linesBuffer += s"Cannot convert configuration to a ${ct.runtimeClass.getName}. Failures are:"
    linesBuffer += failures.prettyPrint(1)
    linesBuffer += ""
    linesBuffer.mkString(System.lineSeparator())
  }

}
