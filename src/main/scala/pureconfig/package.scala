/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
import java.io.{ OutputStream, PrintStream }
import java.nio.file.{ Files, Path }

import com.typesafe.config.ConfigFactory
import pureconfig.conf.RawConfig

import scala.util.{ Failure, Success, Try }

package object pureconfig {

  /**
   * Load a configuration of type [[Config]] from the standard configuration files
   *
   * @return A [[Success]] with the configuration if it is possible to create an instance of type
   *         [[Config]] from the configuration files, else a [[Failure]] with details on why it
   *         isn't possible
   */
  def loadConfig[Config](implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    val rawConfig = conf.typesafeConfigToConfig(ConfigFactory.load())
    loadConfig[Config](rawConfig)(conv)
  }

  /**
   * Load a configuration of type [[Config]] from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A [[Success]] with the configuration if it is possible to create an instance of type
   *         [[Config]] from the configuration files, else a [[Failure]] with details on why it
   *         isn't possible
   */
  def loadConfig[Config](namespace: String)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    val rawConfig = conf.typesafeConfigToConfig(ConfigFactory.load())
    loadConfig[Config](rawConfig, namespace)(conv)
  }

  /**
   * Load a configuration of type [[Config]] from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @return A [[Success]] with the configuration if it is possible to create an instance of type
   *         [[Config]] from the configuration files, else a [[Failure]] with details on why it
   *         isn't possible
   */
  def loadConfig[Config](path: Path)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    val rawConfig = conf.typesafeConfigToConfig(ConfigFactory.load(ConfigFactory.parseFile(path.toFile)))
    loadConfig[Config](rawConfig, "")(conv)
  }

  /**
   * Load a configuration of type [[Config]] from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A [[Success]] with the configuration if it is possible to create an instance of type
   *         [[Config]] from the configuration files, else a [[Failure]] with details on why it
   *         isn't possible
   */
  def loadConfig[Config](path: Path, namespace: String)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    val rawConfig = conf.typesafeConfigToConfig(ConfigFactory.load(ConfigFactory.parseFile(path.toFile)))
    loadConfig[Config](rawConfig, namespace)(conv)
  }

  /** Load a configuration of type [[Config]] from the given [[RawConfig]] */
  def loadConfig[Config](conf: RawConfig)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    loadConfig[Config](conf, "")
  }

  /** Load a configuration of type [[Config]] from the given [[RawConfig]] */
  def loadConfig[Config](conf: RawConfig, namespace: String)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    conv.from(conf, namespace)
  }

  /**
   * Save the given configuration into a property file
   *
   * @param conf The configuration to save
   * @param outputPath Where to write the configuration
   * @param overrideOutputPath Override the path if it already exists
   */
  @throws[IllegalArgumentException]
  def saveConfigAsPropertyFile[Config](conf: Config, outputPath: Path, overrideOutputPath: Boolean = false)(implicit conv: ConfigConvert[Config]): Unit = {
    if (!overrideOutputPath && Files.isRegularFile(outputPath)) {
      throw new IllegalArgumentException(s"Cannot save configuration in file '${outputPath}' because it already exists")
    }
    if (Files isDirectory outputPath) {
      throw new IllegalArgumentException(s"Cannot save configuration in file '${outputPath}' because it already exists and is a directory")
    }

    saveConfigToStream(conf, Files.newOutputStream(outputPath))(conv)
  }

  /**
   * Writes the configuration to the output stream and closes the stream
   *
   * @param conf The configuration to write
   * @param outputStream The stream in which the configuration should be written
   */
  def saveConfigToStream[Config](conf: Config, outputStream: OutputStream)(implicit conv: ConfigConvert[Config]): Unit = {
    val printOutputStream = new PrintStream(outputStream)
    val rawConfig = conv.to(conf, "")
    rawConfig foreach { case (key, value) => printOutputStream.println(s"""$key="$value"""") }
    printOutputStream.close()
  }
}
