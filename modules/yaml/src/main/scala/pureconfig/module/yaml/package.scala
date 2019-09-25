package pureconfig.module

import java.nio.file.Path

import scala.reflect.ClassTag

import pureconfig._
import pureconfig.error._

package object yaml {

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @param path the path of the YAML file to read
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the YAML file, else a `Failure` with details on why it isn't possible
   */
  @deprecated("Use `YamlConfigSource.file(path).load[Config]` instead", "0.12.1")
  def loadYaml[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    YamlConfigSource.file(path).load[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @param path the path of the YAML file to read
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the YAML file, else a `Failure` with details on why it isn't possible
   */
  @deprecated("Use `YamlConfigSource.file(path).at(namespace).load[Config]` instead", "0.12.1")
  def loadYaml[Config](path: Path, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    YamlConfigSource.file(path).at(namespace).load[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given string.
   *
   * @param content the string containing the YAML document
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from `content`, else a `Failure` with details on why it isn't possible
   */
  @deprecated("Use `YamlConfigSource.string(content).load[Config]` instead", "0.12.1")
  def loadYaml[Config](content: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    YamlConfigSource.string(content).load[Config]
  }

  @deprecated("Use `YamlConfigSource.string(content).at(namespace).load[Config]` instead", "0.12.1")
  def loadYaml[Config](content: String, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    YamlConfigSource.string(content).at(namespace).load[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @param path the path of the YAML file to read
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `YamlConfigSource.file(path).loadOrThrow[Config]` instead", "0.12.1")
  def loadYamlOrThrow[Config: ClassTag](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    YamlConfigSource.file(path).loadOrThrow[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given YAML file.
   *
   * @param path the path of the YAML file to read
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `YamlConfigSource.file(path).at(namespace).loadOrThrow[Config]` instead", "0.12.1")
  def loadYamlOrThrow[Config: ClassTag](path: Path, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    YamlConfigSource.file(path).at(namespace).loadOrThrow[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given string.
   *
   * @param content the string containing the YAML document
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `YamlConfigSource.string(content).loadOrThrow[Config]` instead", "0.12.1")
  def loadYamlOrThrow[Config: ClassTag](content: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    YamlConfigSource.string(content).loadOrThrow[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given string.
   *
   * @param content the string containing the YAML document
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `YamlConfigSource.string(content).at(namespace).loadOrThrow[Config]` instead", "0.12.1")
  def loadYamlOrThrow[Config: ClassTag](content: String, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    YamlConfigSource.string(content).at(namespace).loadOrThrow[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document YAML file. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @param path the path of the YAML file to read
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the multi-document YAML file, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `YamlConfigSource.file(path).multiDoc.load[Config]` instead", "0.12.1")
  def loadYamls[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    YamlConfigSource.file(path).multiDoc.load[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document string. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @param content the string containing the YAML documents
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the multi-document string, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `YamlConfigSource.string(content).multiDoc.load[Config]` instead", "0.12.1")
  def loadYamls[Config](content: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    YamlConfigSource.string(content).multiDoc.load[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document YAML file. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @param path the path of the YAML file to read
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `YamlConfigSource.file(path).multiDoc.loadOrThrow[Config]` instead", "0.12.1")
  def loadYamlsOrThrow[Config: ClassTag](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    YamlConfigSource.file(path).multiDoc.loadOrThrow[Config]
  }

  /**
   * Loads a configuration of type `Config` from the given multi-document string. `Config` must have a
   * `ConfigReader` supporting reading from config lists.
   *
   * @param content the string containing the YAML documents
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `YamlConfigSource.string(content).multiDoc.loadOrThrow[Config]` instead", "0.12.1")
  def loadYamlsOrThrow[Config: ClassTag](content: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    YamlConfigSource.string(content).multiDoc.loadOrThrow[Config]
  }
}
