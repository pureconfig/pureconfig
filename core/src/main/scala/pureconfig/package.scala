/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */

import java.io.{ OutputStream, OutputStreamWriter }
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{ Files, Path }

import scala.reflect.ClassTag

import com.typesafe.config.{ Config => TypesafeConfig, _ }
import pureconfig.error._

package object pureconfig {

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `ConfigSource.default.load[Config]` instead", "0.12.0")
  def loadConfig[Config](implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.default.load[Config]

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `ConfigSource.default.at(namespace).load[Config]` instead", "0.12.0")
  def loadConfig[Config](namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.default.at(namespace).load[Config]

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).load[Config]` instead", "0.12.0")
  def loadConfig[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.default(ConfigSource.file(path)).load[Config]

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).at(namespace).load[Config]` instead", "0.12.0")
  def loadConfig[Config](path: Path, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.default(ConfigSource.file(path)).at(namespace).load[Config]

  /** Load a configuration of type `Config` from the given `Config` */
  @deprecated("Use `ConfigSource.fromConfig(conf).load[Config]` instead", "0.12.0")
  def loadConfig[Config](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.fromConfig(conf).load[Config]

  /** Load a configuration of type `Config` from the given `Config` */
  @deprecated("Use `ConfigSource.fromConfig(conf).at(namespace).load[Config]` instead", "0.12.0")
  def loadConfig[Config](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.fromConfig(conf).at(namespace).load[Config]

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).load[Config]` instead", "0.12.0")
  def loadConfigWithFallback[Config](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).load[Config]

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf      Typesafe configuration to load
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  @deprecated("Use `ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).at(namespace).load[Config]` instead", "0.12.0")
  def loadConfigWithFallback[Config](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] =
    ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).at(namespace).load[Config]

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.default.loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigOrThrow[Config: ClassTag](implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.default.loadOrThrow[Config]

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.default.at(namespace).loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigOrThrow[Config: ClassTag](namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.default.at(namespace).loadOrThrow[Config]

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigOrThrow[Config: ClassTag](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.default(ConfigSource.file(path)).loadOrThrow[Config]

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).at(namespace).loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigOrThrow[Config: ClassTag](path: Path, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.default(ConfigSource.file(path)).at(namespace).loadOrThrow[Config]

  /**
   * Load a configuration of type `Config` from the given `Config`
   *
   * @param conf Typesafe configuration to load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.fromConfig(conf).loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigOrThrow[Config: ClassTag](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.fromConfig(conf).loadOrThrow[Config]

  /**
   * Load a configuration of type `Config` from the given `Config`
   *
   * @param conf      Typesafe configuration to load
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.fromConfig(conf).at(namespace).loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigOrThrow[Config: ClassTag](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.fromConfig(conf).at(namespace).loadOrThrow[Config]

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigWithFallbackOrThrow[Config: ClassTag](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).loadOrThrow[Config]

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf      Typesafe configuration to load
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Use `ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).at(namespace).loadOrThrow[Config]` instead", "0.12.0")
  def loadConfigWithFallbackOrThrow[Config: ClassTag](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config =
    ConfigSource.fromConfig(conf).withFallback(ConfigSource.default).at(namespace).loadOrThrow[Config]

  /**
   * Save the given configuration into a property file
   *
   * @param conf               The configuration to save
   * @param outputPath         Where to write the configuration
   * @param overrideOutputPath Override the path if it already exists
   * @param options            the config rendering options
   */
  @throws[IllegalArgumentException]
  def saveConfigAsPropertyFile[Config](
    conf: Config,
    outputPath: Path,
    overrideOutputPath: Boolean = false,
    options: ConfigRenderOptions = ConfigRenderOptions.defaults())(implicit writer: Derivation[ConfigWriter[Config]]): Unit = {

    if (!overrideOutputPath && Files.isRegularFile(outputPath)) {
      throw new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists")
    }
    if (Files isDirectory outputPath) {
      throw new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists and is a directory")
    }

    saveConfigToStream(conf, Files.newOutputStream(outputPath), options)
  }

  /**
   * Writes the configuration to the output stream and closes the stream
   *
   * @param conf         The configuration to write
   * @param outputStream The stream in which the configuration should be written
   * @param options      the config rendering options
   */
  def saveConfigToStream[Config](
    conf: Config,
    outputStream: OutputStream,
    options: ConfigRenderOptions = ConfigRenderOptions.defaults())(implicit writer: Derivation[ConfigWriter[Config]]): Unit = {

    // HOCON requires UTF-8:
    // https://github.com/lightbend/config/blob/master/HOCON.md#unchanged-from-json
    val printOutputStream = new OutputStreamWriter(outputStream, UTF_8)
    val rawConf = writer.value.to(conf)
    printOutputStream.write(rawConf.render(options))
    printOutputStream.close()
  }

  /**
   * Loads `files` in order, allowing values in later files to backstop missing values from prior, and converts them
   * into a `Config`.
   *
   * This is a convenience method which enables having default configuration which backstops local configuration.
   *
   * The behavior of the method if an element of `files` references a file which doesn't exist or can't be read is
   * defined by the `failOnReadError` flag. With `failOnReadError = false`, such files will silently be ignored while
   * otherwise they would yield a failure (a `Left` value).
   *
   * @param files           Files ordered in decreasing priority containing part or all of a `Config`
   * @param failOnReadError Where to return an error if any files fail to read
   * @param namespace       the base namespace from which the configuration should be load
   */
  @deprecated("Construct a custom `ConfigSource` pipeline instead", "0.12.0")
  def loadConfigFromFiles[Config](files: Traversable[Path], failOnReadError: Boolean = false, namespace: String = "")(implicit reader: Derivation[ConfigReader[Config]]): ConfigReader.Result[Config] = {
    ConfigSource.default(
      files.map(ConfigSource.file)
        .map(cs => if (failOnReadError) cs else cs.optional)
        .foldLeft(ConfigSource.empty)(_.withFallback(_)))
      .at(namespace)
      .load[Config]
  }

  /**
   * @see [[loadConfigFromFiles]]
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  @deprecated("Construct a custom `ConfigSource` pipeline instead", "0.12.0")
  def loadConfigFromFilesOrThrow[Config: ClassTag](files: Traversable[Path])(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    ConfigSource.default(
      files.map(ConfigSource.file(_).optional)
        .foldLeft(ConfigSource.empty)(_.withFallback(_)))
      .loadOrThrow[Config]
  }
}
