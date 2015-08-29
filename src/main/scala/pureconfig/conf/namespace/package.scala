/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig.conf

package object namespace {

  /**
   * Create a new namepace from the parent namespace and a key
   *
   * {{{
   *   scala> makeNamespace("", "foo")
   *   res0: String = "foo"
   *   scala> makeNamespace("foo", "bar")
   *   res1: String = "foo.bar"
   * }}}
   */
  def makeNamespace(namespace: String, key: String): String = namespace match {
    case "" => key
    case _ => namespace + "." + key
  }
}
