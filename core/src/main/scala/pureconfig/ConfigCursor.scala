package pureconfig

import com.typesafe.config._

case class ConfigCursor(value: ConfigValue, path: List[String])
