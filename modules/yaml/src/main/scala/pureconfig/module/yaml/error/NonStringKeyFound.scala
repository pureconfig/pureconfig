package pureconfig.module.yaml.error

import pureconfig.error.ConfigReaderFailure

case class NonStringKeyFound(value: String, keyType: String) extends ConfigReaderFailure {
  def description = s"Cannot read YAML key '$value' (with type $keyType). PureConfig only supports string keys."
  def location = None
}
