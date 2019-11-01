package pureconfig.module

import java.io.OutputStream
import java.nio.file.Path

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import com.typesafe.config.{ ConfigRenderOptions, Config => TypesafeConfig }
import pureconfig._
import pureconfig.error.ConfigReaderException

package object catseffect {

  @deprecated("Root will be treated as the default namespace", "0.12.0")
  val defaultNameSpace = ""

  private def configToF[F[_], A](getConfig: () => ConfigReader.Result[A])(implicit F: Sync[F], ct: ClassTag[A]): F[A] = {
    val delayedLoad = F.delay {
      getConfig().leftMap[Throwable](ConfigReaderException[A])
    }
    delayedLoad.rethrow
  }

  implicit class CatsEffectConfigSource(val cs: ConfigSource) extends AnyVal {
    def loadF[F[_], A](implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
      configToF(() => cs.load[A])

  
    def configF[F[_]](implicit F: Sync[F]): F[Config] =
      F.delay(cs.config().leftMap(ConfigReaderException[Config])).rethrow
}

  /**
   * Load a configuration of type `A` from the standard configuration files
   *
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  def loadConfigF[F[_], A](implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    ConfigSource.default.loadF[F, A]

  /**
   * Load a configuration of type `A` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  @deprecated("Use `ConfigSource.default.at(namespace).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](namespace: String)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    ConfigSource.default.at(namespace).loadF[F, A]

  /**
   * Load a configuration of type `A` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param path the path of the configuration file from which to load
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration file, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](path: Path)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    ConfigSource.default(ConfigSource.file(path)).loadF[F, A]

  /**
   * Load a configuration of type `A` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param path the path of the configuration file from which to load
   * @param namespace the base namespace from which the configuration should be load
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration file, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).at(namespace).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](path: Path, namespace: String)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    ConfigSource.default(ConfigSource.file(path)).at(namespace).loadF[F, A]

  /**
   * Load a configuration of type `A` from the given `Config`
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration object, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  @deprecated("Use `ConfigSource.fromConfig(conf).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](conf: TypesafeConfig)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    ConfigSource.fromConfig(conf).loadF[F, A]

  /**
   * Load a configuration of type `A` from the given `Config`
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration object, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  @deprecated("Use `ConfigSource.fromConfig(conf).at(namespace).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](conf: TypesafeConfig, namespace: String)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    ConfigSource.fromConfig(conf).at(namespace).loadF[F, A]

  /**
   * Save the given configuration into a property file
   *
   * @param conf The configuration to save
   * @param outputPath Where to write the configuration
   * @param overrideOutputPath Override the path if it already exists
   * @param options the config rendering options
   * @return The return action will save out the supplied configuration upon invocation
   */
  def saveConfigAsPropertyFileF[F[_], A](
    conf: A,
    outputPath: Path,
    overrideOutputPath: Boolean = false,
    options: ConfigRenderOptions = ConfigRenderOptions.defaults())(implicit F: Sync[F], writer: Derivation[ConfigWriter[A]]): F[Unit] = F.delay {
    pureconfig.saveConfigAsPropertyFile(conf, outputPath, overrideOutputPath, options)
  }

  /**
   * Writes the configuration to the output stream and closes the stream
   *
   * @param conf The configuration to write
   * @param outputStream The stream in which the configuration should be written
   * @param options the config rendering options
   * @return The return action will save out the supplied configuration upon invocation
   */
  def saveConfigToStreamF[F[_], A](
    conf: A,
    outputStream: OutputStream,
    options: ConfigRenderOptions = ConfigRenderOptions.defaults())(implicit F: Sync[F], writer: Derivation[ConfigWriter[A]]): F[Unit] = F.delay {
    pureconfig.saveConfigToStream(conf, outputStream, options)
  }

  /**
   * Loads `files` in order, allowing values in later files to backstop missing values from prior, and converts them into a `A`.
   *
   * This is a convenience method which enables having default configuration which backstops local configuration.
   *
   * Note: If an element of `files` references a file which doesn't exist or can't be read, it will silently be ignored.
   *
   * @param files Files ordered in decreasing priority containing part or all of a `A`. Must not be empty.
   */
  @deprecated("Construct a custom `ConfigSource` pipeline instead", "0.12.0")
  def loadConfigFromFilesF[F[_], A](files: NonEmptyList[Path])(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    ConfigSource.default(
      files.map(ConfigSource.file(_).optional)
        .foldLeft(ConfigSource.empty)(_.withFallback(_)))
      .loadF[F, A]
}
