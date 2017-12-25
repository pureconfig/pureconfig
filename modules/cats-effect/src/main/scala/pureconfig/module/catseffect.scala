package pureconfig.module

import java.io.OutputStream
import java.nio.file.Path

import scala.language.higherKinds
import pureconfig.error.{ ConfigReaderException, ConfigReaderFailures }
import pureconfig.{ ConfigReader, ConfigWriter, Derivation }
import cats.effect.Sync
import cats.implicits._
import cats.data.NonEmptyList

import scala.reflect.ClassTag

package object catseffect {

  private val defaultNameSpace = ""

  private def configToF[F[_], A](getConfig: () => Either[ConfigReaderFailures, A])(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] = {
    val delayedLoad = F.delay {
      getConfig().leftMap(ConfigReaderException[A])
    }
    delayedLoad.flatMap(F.fromEither)
  }

  /**
   * Load a configuration of type `A` from the standard configuration files
   *
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  def loadConfigF[F[_], A](implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadConfigF[F, A](defaultNameSpace)

  /**
   * Load a configuration of type `A` from the standard configuration files
   *
   * @param namespace the base namespace from which the configuration should be load
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  def loadConfigF[F[_], A](namespace: String)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] = {
    configToF(() => pureconfig.loadConfig[A](namespace))
  }

  /**
   * Load a configuration of type `A` from the given file. Note that standard configuration
   * files are still loaded but can be overridden from the given configuration file
   *
   * @param path the path of the configuration file from which to load
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration file, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  def loadConfigF[F[_], A](path: Path)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadConfigF[F, A](path, defaultNameSpace)

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
  def loadConfigF[F[_], A](path: Path, namespace: String)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] = {
    configToF(() => pureconfig.loadConfig[A](path, namespace))
  }

  /**
   * Save the given configuration into a property file
   *
   * @param conf The configuration to save
   * @param outputPath Where to write the configuration
   * @param overrideOutputPath Override the path if it already exists
   * @return The return action will save out the supplied configuration upon invocation
   */
  def saveConfigAsPropertyFileF[F[_], A](conf: A, outputPath: Path, overrideOutputPath: Boolean = false)(implicit F: Sync[F], writer: Derivation[ConfigWriter[A]]): F[Unit] = F.delay {
    pureconfig.saveConfigAsPropertyFile(conf, outputPath, overrideOutputPath)
  }

  /**
   * Writes the configuration to the output stream and closes the stream
   *
   * @param conf The configuration to write
   * @param outputStream The stream in which the configuration should be written
   * @return The return action will save out the supplied configuration upon invocation
   */
  def saveConfigToStreamF[F[_], A](conf: A, outputStream: OutputStream)(implicit F: Sync[F], writer: Derivation[ConfigWriter[A]]): F[Unit] = F.delay {
    pureconfig.saveConfigToStream(conf, outputStream)
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
  def loadConfigFromFilesF[F[_], A](files: NonEmptyList[Path])(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    configToF(() => pureconfig.loadConfigFromFiles(files.toList))
}
