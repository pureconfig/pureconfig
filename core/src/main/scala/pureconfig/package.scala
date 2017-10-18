/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Mario Pastorelli
 */
import java.io.{ OutputStream, PrintStream }
import java.nio.file.{ Files, Path }

import scala.annotation.tailrec
import scala.reflect.ClassTag

import com.typesafe.config.{ Config => TypesafeConfig, _ }
import pureconfig.ConvertHelpers._
import pureconfig.backend.ConfigFactoryWrapper._
import pureconfig.backend.PathUtil
import pureconfig.backend.PathUtil._
import pureconfig.error._

package object pureconfig {

  // retrieves a value from a namespace, returning a failure if:
  //   - one of the parent keys doesn't exist or isn't an object;
  //   - `allowNullLeaf` is false and the leaf key doesn't exist.
  private[this] def getValue(conf: TypesafeConfig, namespace: String, allowNullLeaf: Boolean): Either[ConfigReaderFailures, ConfigValue] = {
    @tailrec def getValue(cv: ConfigValue, path: List[String], curr: List[String]): Either[ConfigReaderFailures, ConfigValue] = {
      (cv, path) match {
        case (_, Nil) => Right(cv)

        case (co: ConfigObject, key :: remaining) =>
          co.get(key) match {
            case null if remaining.nonEmpty || !allowNullLeaf =>
              fail(KeyNotFound(joinPath((key :: curr).reverse), ConfigValueLocation(cv.origin()), Set.empty))
            case childCv =>
              getValue(childCv, remaining, key :: curr)
          }

        case _ => fail(WrongType(
          cv.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(cv.origin()), joinPath(curr.reverse)))
      }
    }

    // we're not expecting any exception here, this `try` is just for extra safety
    try getValue(conf.root(), splitPath(namespace), Nil) catch {
      case ex: ConfigException => fail(ThrowableFailure(ex, ConfigValueLocation(ex.origin()), ""))
    }
  }

  // loads a value from a config in a given namespace. All `loadConfig` methods _must_ use this method to get correct
  // namespace handling, both in the values to load and in the error messages.
  private[this] def loadValue[A](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[A]]): Either[ConfigReaderFailures, A] = {
    getValue(conf, namespace, reader.value.isInstanceOf[AllowMissingKey]).right.flatMap { cv =>
      val cur = ConfigCursor(cv, PathUtil.splitPath(namespace))
      reader.value.from(cur)
    }
  }

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] =
    loadConfig("")

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- load().right
      config <- loadValue[Config](rawConfig, namespace).right
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
  def loadConfig[Config](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] =
    loadConfig(path, "")

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfig[Config](path: Path, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- loadFile(path).right
      config <- loadValue[Config](rawConfig, namespace).right
    } yield config
  }

  /** Load a configuration of type `Config` from the given `Config` */
  def loadConfig[Config](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] =
    loadValue(conf, "")

  /** Load a configuration of type `Config` from the given `Config` */
  def loadConfig[Config](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] =
    loadValue(conf, namespace)

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfigWithFallback[Config](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] =
    loadConfigWithFallback(conf, "")

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @param namespace the base namespace from which the configuration should be load
   * @return A `Success` with the configuration if it is possible to create an instance of type
   *         `Config` from the configuration files, else a `Failure` with details on why it
   *         isn't possible
   */
  def loadConfigWithFallback[Config](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] = {
    for {
      _ <- invalidateCaches().right
      rawConfig <- load().right
      config <- loadConfig[Config](conf.withFallback(rawConfig), namespace).right
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
  def loadConfigOrThrow[Config: ClassTag](implicit reader: Derivation[ConfigReader[Config]]): Config = {
    getResultOrThrow(loadConfig[Config])
  }

  /**
   * Load a configuration of type `Config` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config: ClassTag](namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    getResultOrThrow(loadConfig[Config](namespace))
  }

  /**
   * Load a configuration of type `Config` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config: ClassTag](path: Path)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
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
  def loadConfigOrThrow[Config: ClassTag](path: Path, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    getResultOrThrow(loadConfig[Config](path, namespace))
  }

  /**
   * Load a configuration of type `Config` from the given `Config`
   *
   * @param conf Typesafe configuration to load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigOrThrow[Config: ClassTag](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
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
  def loadConfigOrThrow[Config: ClassTag](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    getResultOrThrow(loadConfig[Config](conf, namespace))
  }

  /**
   * Load a configuration of type `Config` from the given `Config`, falling back to the default configuration
   *
   * @param conf Typesafe configuration to load
   * @return the configuration
   */
  @throws[ConfigReaderException[_]]
  def loadConfigWithFallbackOrThrow[Config: ClassTag](conf: TypesafeConfig)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
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
  def loadConfigWithFallbackOrThrow[Config: ClassTag](conf: TypesafeConfig, namespace: String)(implicit reader: Derivation[ConfigReader[Config]]): Config = {
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
  def saveConfigAsPropertyFile[Config](conf: Config, outputPath: Path, overrideOutputPath: Boolean = false)(implicit writer: Derivation[ConfigWriter[Config]]): Unit = {
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
  def saveConfigToStream[Config](conf: Config, outputStream: OutputStream)(implicit writer: Derivation[ConfigWriter[Config]]): Unit = {
    val printOutputStream = new PrintStream(outputStream)
    val rawConf = writer.value.to(conf)
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
  def loadConfigFromFiles[Config](files: Traversable[Path])(implicit reader: Derivation[ConfigReader[Config]]): Either[ConfigReaderFailures, Config] = {
    if (files.isEmpty) {
      ConfigConvert.failWithThrowable[Config](new IllegalArgumentException("The config files to load must not be empty."))(None)
    } else {
      files
        .map(parseFile)
        .foldLeft[Either[ConfigReaderFailures, Seq[TypesafeConfig]]](Right(Seq())) {
          case (c1, Left(failures)) if failures.toList.exists(_.isInstanceOf[CannotReadFile]) => c1
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
  def loadConfigFromFilesOrThrow[Config: ClassTag](files: Traversable[Path])(implicit reader: Derivation[ConfigReader[Config]]): Config = {
    getResultOrThrow[Config](loadConfigFromFiles[Config](files))
  }
}
