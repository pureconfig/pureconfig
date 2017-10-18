package pureconfig.error

import java.net.URL

import com.typesafe.config.{ ConfigOrigin, ConfigValue }

/**
 * The file system location of a ConfigValue, represented by a url and a line
 * number
 *
 * @param url the URL describing the origin of the ConfigValue
 * @param lineNumber the line number (starting at 0), where the given
 *                   ConfigValue definition starts
 */
case class ConfigValueLocation(url: URL, lineNumber: Int) {
  def description: String = s"($url:$lineNumber)"
}

object ConfigValueLocation {
  /**
   * Helper method to create an optional ConfigValueLocation from a ConfigValue.
   * Since it might not be possible to derive a ConfigValueLocation from a
   * ConfigValue, this method returns Option.
   *
   * @param cv the ConfigValue to derive the location from
   *
   * @return a Some with the location of the ConfigValue, or None if it is not
   *         possible to derive a location. It is not possible to derive
   *         locations from ConfigValues that are not in files or for
   *         ConfigValues that are null.
   */
  def apply(cv: ConfigValue): Option[ConfigValueLocation] =
    Option(cv).flatMap(v => apply(v.origin()))

  /**
   * Helper method to create an optional ConfigValueLocation from a ConfigOrigin.
   * Since it might not be possible to derive a ConfigValueLocation from a
   * ConfigOrigin, this method returns Option.
   *
   * @param co the ConfigOrigin to derive the location from
   *
   * @return a Some with the location of the ConfigOrigin, or None if it is not
   *         possible to derive a location. It is not possible to derive
   *         locations from ConfigOrigin that are not in files or for
   *         ConfigOrigin that are null.
   */
  def apply(co: ConfigOrigin): Option[ConfigValueLocation] =
    Option(co).flatMap { origin =>
      if (origin.url != null && origin.lineNumber != -1)
        Some(ConfigValueLocation(origin.url, origin.lineNumber))
      else
        None
    }
}
