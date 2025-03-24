package pureconfig.module.yaml.error

import com.typesafe.config.ConfigOrigin

import pureconfig.error.ConfigReaderFailure

case class UnsupportedYamlType(value: String, keyType: String) extends ConfigReaderFailure {
  def description = s"Cannot read YAML value '$value' (with unsupported type $keyType)."
  def origin: Option[ConfigOrigin] = None
}
