/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
import java.io.{ OutputStream, PrintStream }
import java.nio.file.{ Files, Path }

import com.typesafe.config.{ ConfigException, Config => TypesafeConfig }
import pureconfig.error._
import pureconfig.ConfigConvert.improveFailures
import pureconfig.backend.ConfigFactoryWrapper.{ invalidateCaches, load, loadFile, parseFile }
import scala.reflect.ClassTag

package object pureconfig {

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- load().right
      config <- loadConfig[Config](rawConfig).right
    } yield config
  }

  private[this] def getConfigSafe(conf: TypesafeConfig, namespace: String): Either[ConfigReaderFailures, TypesafeConfig] = {
    try Right(conf.getConfig(namespace)) catch {
      case ex: ConfigException.Missing =>
        Left(ConfigReaderFailures(KeyNotFound(namespace, ConfigValueLocation(ex.origin()), Set.empty)))
    }
  }

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](namespace: String)(implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- load().right
      scopedConfig <- getConfigSafe(rawConfig, namespace).right
      config <- improveFailures[Config](loadConfig[Config](scopedConfig), namespace, ConfigValueLocation(scopedConfig.root())).right
    } yield config
  }

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](path: Path)(implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- loadFile(path).right
      config <- loadConfig[Config](rawConfig).right
    } yield config
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
  def loadConfig[Config](path: Path, namespace: String)(implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- loadFile(path).right
      scopedConfig <- getConfigSafe(rawConfig, namespace).right
      config <- improveFailures[Config](loadConfig[Config](scopedConfig), namespace, ConfigValueLocation(scopedConfig.root())).right
    } yield config
  }

  /** Load a configuration of type `Config` from the given `Config` */
  def loadConfig[Config](conf: TypesafeConfig)(implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] =
    reader.from(conf.root())

  /** Load a configuration of type `Config` from the given `Config` */
  def loadConfig[Config](conf: TypesafeConfig, namespace: String)(implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] = {
    getConfigSafe(conf, namespace).right.flatMap { scopedConfig =>
      improveFailures[Config](reader.from(scopedConfig.root()), namespace, ConfigValueLocation(scopedConfig.root()))
    }
  }

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfigWithFallback[Config](conf: TypesafeConfig)(implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- load().right
      config <- loadConfig[Config](conf.withFallback(rawConfig)).right
    } yield config
  }

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfigWithFallback[Config](conf: TypesafeConfig, namespace: String)(implicit reader: ConfigReader[Config]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- load().right
      scopedConfig <- getConfigSafe(rawConfig, namespace).right
      config <- loadConfig[Config](conf.withFallback(scopedConfig), namespace).right
    } yield config
  }

  private[this] def getResultOrThrow[Config](failuresOrResult: Either[ConfigReaderFailures, Config])(implicit ct: ClassTag[Config]): Config = {
    failuresOrResult match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config](implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfig[Config])
  }

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config](namespace: String)(implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfig[Config](namespace))
  }

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config](path: Path)(implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfig[Config](path))
  }

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config](path: Path, namespace: String)(implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfig[Config](path, namespace))
  }

  /**
   * Load a configuration of type `Config` from the given `Config`
   *
   * @param conf Typesafe configuration to load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config](conf: TypesafeConfig)(implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfig[Config](conf))
  }

  /**
   * Load a configuration of type `Config` from the given `Config`
   *
   * @param conf Typesafe configuration to load
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config](conf: TypesafeConfig, namespace: String)(implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfig[Config](conf, namespace))
  }

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigWithFallbackOrThrow[Config](conf: TypesafeConfig)(implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfigWithFallback[Config](conf))
  }

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigWithFallbackOrThrow[Config](conf: TypesafeConfig, namespace: String)(implicit reader: ConfigReader[Config], ct: ClassTag[Config]): Config = {
    getResultOrThrow(loadConfigWithFallback[Config](conf, namespace))
  }

  /**
   * Save the given configuration into a property file
   *
   * @param conf The configuration to save
   * @param outputPath Where to write the configuration
   * @param overrideOutputPath Override the path if it already exists
   */
  @throws[IllegalArgumentException]
  def saveConfigAsPropertyFile[Config](conf: Config, outputPath: Path, overrideOutputPath: Boolean = false)(implicit writer: ConfigWriter[Config]): Unit = {
    if (!overrideOutputPath && Files.isRegularFile(outputPath)) {
      throw new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists")
    }
    if (Files isDirectory outputPath) {
      throw new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists and is a directory")
    }

    saveConfigToStream(conf, Files.newOutputStream(outputPath))
  }

  /**
   * Writes the configuration to the output stream and closes the stream
   *
   * @param conf The configuration to write
   * @param outputStream The stream in which the configuration should be written
   */
  def saveConfigToStream[Config](conf: Config, outputStream: OutputStream)(implicit writer: ConfigWriter[Config]): Unit = {
    val printOutputStream = new PrintStream(outputStream)
    val rawConf = writer.to(conf)
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
  def loadConfigFromFiles[Config: ConfigReader](files: Traversable[Path]): Either[ConfigReaderFailures, Config] = {
    if (files.isEmpty) {
      ConfigConvert.failWithThrowable[Config](new IllegalArgumentException("The config files to load must not be empty."))(None)
    } else {
      files
        .map(parseFile)
        .foldLeft[Either[ConfigReaderFailures, Seq[TypesafeConfig]]](Right(Seq())) {
          case (c1, Left(ConfigReaderFailures(_: CannotReadFile, Nil))) => c1
          case (c1, c2) => ConfigConvert.combineResults(c1, c2)(_ :+ _)
        }
        .right.map(_.reduce(_.withFallback(_)).resolve)
        .right.flatMap(loadConfig[Config])
    }
  }

  /**
   * @see [[loadConfigFromFiles]]
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigFromFilesOrThrow[Config: ConfigReader](files: Traversable[Path])(implicit ct: ClassTag[Config]): Config = {
    getResultOrThrow[Config](loadConfigFromFiles[Config](files))
  }
}
