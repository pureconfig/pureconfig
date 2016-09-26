/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
import java.io.{ OutputStream, PrintStream }
import java.nio.file.{ Files, Path }

import com.typesafe.config.{ Config => TypesafeConfig, ConfigFactory }

import scala.util.Try

package object pureconfig {

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    loadConfig[Config](ConfigFactory.load())(conv)
  }

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](namespace: String)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    loadConfig[Config](ConfigFactory.load().getConfig(namespace))(conv)
  }

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](path: Path)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    loadConfig[Config](ConfigFactory.load(ConfigFactory.parseFile(path.toFile)))(conv)
  }

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](path: Path, namespace: String)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    loadConfig[Config](ConfigFactory.load(ConfigFactory.parseFile(path.toFile)).getConfig(namespace))(conv)
  }

  /** Load a configuration of type `Config` from the given `Config` */
  def loadConfig[Config](conf: TypesafeConfig)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    conv.from(conf.root())
  }

  /** Load a configuration of type `Config` from the given `Config` */
  def loadConfig[Config](conf: TypesafeConfig, namespace: String)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    conv.from(conf.getConfig(namespace).root())
  }

  /**
    * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration */
  def loadConfigWithFallBack[Config](conf: TypesafeConfig)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    loadConfig[Config](conf.withFallback(ConfigFactory.load()))
  }

  /**
    * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration */
  def loadConfigWithFallBack[Config](conf: TypesafeConfig, namespace: String)(implicit conv: ConfigConvert[Config]): Try[Config] = {
    ConfigFactory.invalidateCaches()
    loadConfig[Config](conf.withFallback(ConfigFactory.load()), namespace)
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
      throw new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists")
    }
    if (Files isDirectory outputPath) {
      throw new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists and is a directory")
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
    val rawConf = conv.to(conf)
    printOutputStream.print(rawConf.render())
    printOutputStream.close()
  }

  /**
   * Loads `files` in order, allowing values in later files to backstop missing values from prior, and converts them into a `Config`.
   *
   * This is a convenience method which enables having default configuration which backstops local configuration.
   *
   * Note: If an element of `files` references a file which doesn't exist or can't be read, it will silently be ignored.
   *
   * @param files Files ordered in decreasing priority containing part or all of a `Config`. Must not be empty.
   */
  def loadConfigFromFiles[Config: ConfigConvert](files: Traversable[java.io.File]): Try[Config] = {
    if (files.isEmpty) {
      new scala.util.Failure(new IllegalArgumentException("The config files to load must not be empty."))
    } else {
      val resolvedTypesafeConfig = files
        .map(ConfigFactory.parseFile)
        .reduce(_.withFallback(_))
        .resolve
      loadConfig[Config](resolvedTypesafeConfig)
    }
  }
}
