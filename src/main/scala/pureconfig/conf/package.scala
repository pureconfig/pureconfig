/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
package pureconfig

import com.typesafe.config.{ Config => TypesafeConfig, ConfigList }
import scala.collection.JavaConversions._

package object conf {

  /**
   * A [[RawConfig]] is a [[Map]] from namespaces to a raw (string) value.
   *
   * @note This type could change in future for more efficient configuration storage. Probably it
   *       will be an alias for [[TypesafeConfig]].
   */
  type RawConfig = Map[String, String]

  /**
   * Convert a typesafe configuration to the [[RawConfig]] to be used. Working with [[RawConfig]]
   * tends to be easier because we can easily create [[RawConfig]] from any type.
   *
   * @param conf The configuraton to convert
   * @return The configuration converted
   */
  def typesafeConfigToConfig(conf: TypesafeConfig): RawConfig = {
    conf.entrySet().map { entry =>
      val key = entry.getKey
      val value = entry.getValue match {
        case list: ConfigList => list.unwrapped().mkString(", ")
        case _ => conf.getString(key)
      }
      key -> value
    }.toMap
  }
}
