package pureconfig.module.yaml.error

import pureconfig.error.ConfigReaderFailure

case class UnsupportedYamlType(value: String, keyType: String) extends ConfigReaderFailure {
  def description(indentSize: Int) = s"Cannot read YAML value '$value' (with unsupported type $keyType)."
  def origin = None
}
