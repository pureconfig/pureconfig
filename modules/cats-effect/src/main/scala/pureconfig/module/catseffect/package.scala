package pureconfig.module

import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.data.{EitherT, NonEmptyList}
import cats.effect.{Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import com.typesafe.config.{ConfigRenderOptions, Config => TypesafeConfig}
import pureconfig._
import pureconfig.error.ConfigReaderException

package object catseffect {

  @deprecated("Root will be treated as the default namespace", "0.12.0")
  val defaultNameSpace = ""

  /** Load a configuration of type `A` from a config source
    *
    * @param cs the config source from where the configuration will be loaded
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration source, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  @deprecated("Use `loadF[F, A](cs, blocker)` instead", "0.12.3")
  def loadF[F[_], A](
      cs: ConfigSource
  )(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] = {
    val delayedLoad = F.delay {
      cs.load[A].leftMap[Throwable](ConfigReaderException[A])
    }
    delayedLoad.rethrow
  }

  /** Load a configuration of type `A` from a config source
    *
    * @param cs the config source from where the configuration will be loaded
    * @param blocker the blocking context which will be used to load the configuration.
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration source, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  def loadF[F[_], A](
      cs: ConfigSource,
      blocker: Blocker
  )(implicit F: Sync[F], csf: ContextShift[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    EitherT(blocker.delay(cs.cursor()))
      .subflatMap(reader.value.from)
      .leftMap(ConfigReaderException[A])
      .rethrowT

  /** Load a configuration of type `A` from the standard configuration files
    *
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  @deprecated("Use `loadConfigF[F, A](blocker)` instead", "0.12.3")
  def loadConfigF[F[_], A](implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadF[F, A](ConfigSource.default)

  /** Load a configuration of type `A` from the standard configuration files
    *
    * @param blocker the blocking context which will be used to load the configuration.
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  def loadConfigF[F[_], A](
      blocker: Blocker
  )(implicit F: Sync[F], csf: ContextShift[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadF(ConfigSource.default, blocker)

  /** Load a configuration of type `A` from the standard configuration files
    *
    * @param namespace the base namespace from which the configuration should be load
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  @deprecated("Use `ConfigSource.default.at(namespace).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](
      namespace: String
  )(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadF[F, A](ConfigSource.default.at(namespace))

  /** Load a configuration of type `A` from the given file. Note that standard configuration
    * files are still loaded but can be overridden from the given configuration file
    *
    * @param path the path of the configuration file from which to load
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration file, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](
      path: Path
  )(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadF[F, A](ConfigSource.default(ConfigSource.file(path)))

  /** Load a configuration of type `A` from the given file. Note that standard configuration
    * files are still loaded but can be overridden from the given configuration file
    *
    * @param path the path of the configuration file from which to load
    * @param namespace the base namespace from which the configuration should be load
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration file, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  @deprecated("Use `ConfigSource.default(ConfigSource.file(path)).at(namespace).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](path: Path, namespace: String)(implicit
      F: Sync[F],
      reader: Derivation[ConfigReader[A]],
      ct: ClassTag[A]
  ): F[A] =
    loadF[F, A](ConfigSource.default(ConfigSource.file(path)).at(namespace))

  /** Load a configuration of type `A` from the given `Config`
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration object, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  @deprecated("Use `ConfigSource.fromConfig(conf).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](
      conf: TypesafeConfig
  )(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadF[F, A](ConfigSource.fromConfig(conf))

  /** Load a configuration of type `A` from the given `Config`
    * @return The returned action will complete with `A` if it is possible to create an instance of type
    *         `A` from the configuration object, or fail with a ConfigReaderException which in turn contains
    *         details on why it isn't possible
    */
  @deprecated("Use `ConfigSource.fromConfig(conf).at(namespace).loadF[F, A]` instead", "0.12.0")
  def loadConfigF[F[_], A](conf: TypesafeConfig, namespace: String)(implicit
      F: Sync[F],
      reader: Derivation[ConfigReader[A]],
      ct: ClassTag[A]
  ): F[A] =
    loadF[F, A](ConfigSource.fromConfig(conf).at(namespace))

  /** Save the given configuration into a property file
    *
    * @param conf The configuration to save
    * @param outputPath Where to write the configuration
    * @param overrideOutputPath Override the path if it already exists
    * @param options the config rendering options
    * @return The return action will save out the supplied configuration upon invocation
    */
  @deprecated(
    "Use `blockingSaveConfigAsPropertyFileF[IO, A](conf, outputPat, blocker, overrideOutputPath, options)` instead",
    "0.12.3"
  )
  def saveConfigAsPropertyFileF[F[_], A](
      conf: A,
      outputPath: Path,
      overrideOutputPath: Boolean = false,
      options: ConfigRenderOptions = ConfigRenderOptions.defaults()
  )(implicit F: Sync[F], writer: Derivation[ConfigWriter[A]]): F[Unit] =
    F.delay {
      pureconfig.saveConfigAsPropertyFile(conf, outputPath, overrideOutputPath, options)
    }

  /** Save the given configuration into a property file
    *
    * @param conf The configuration to save
    * @param outputPath Where to write the configuration
    * @param blocker the blocking context which will be used to load the configuration.
    * @param overrideOutputPath Override the path if it already exists
    * @param options the config rendering options
    * @return The return action will save out the supplied configuration upon invocation
    */
  def blockingSaveConfigAsPropertyFileF[F[_], A](
      conf: A,
      outputPath: Path,
      blocker: Blocker,
      overrideOutputPath: Boolean = false,
      options: ConfigRenderOptions = ConfigRenderOptions.defaults()
  )(implicit F: Sync[F], csf: ContextShift[F], writer: Derivation[ConfigWriter[A]]): F[Unit] = {
    val fileAlreadyExists =
      F.raiseError(
        new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists")
      )

    val fileIsDirectory =
      F.raiseError(
        new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it is a directory")
      )

    val check =
      F.suspend {
        if (!overrideOutputPath && Files.isRegularFile(outputPath)) fileAlreadyExists
        else if (Files.isDirectory(outputPath)) fileIsDirectory
        else F.unit
      }

    val outputStream =
      Resource.make(blocker.delay(Files.newOutputStream(outputPath))) { os =>
        blocker.delay(os.close())
      }

    blocker.blockOn(check) >> outputStream.use { os =>
      blockingSaveConfigToStreamF(conf, os, blocker, options)
    }
  }

  /** Writes the configuration to the output stream and closes the stream
    *
    * @param conf The configuration to write
    * @param outputStream The stream in which the configuration should be written
    * @param options the config rendering options
    * @return The return action will save out the supplied configuration upon invocation
    */
  @deprecated("Use `blockingSaveConfigToStreamF[IO, A](conf, outputStream, blocker, options)` instead", "0.12.3")
  def saveConfigToStreamF[F[_], A](
      conf: A,
      outputStream: OutputStream,
      options: ConfigRenderOptions = ConfigRenderOptions.defaults()
  )(implicit F: Sync[F], writer: Derivation[ConfigWriter[A]]): F[Unit] =
    F.delay {
      pureconfig.saveConfigToStream(conf, outputStream, options)
    }

  /** Writes the configuration to the output stream and closes the stream
    *
    * @param conf The configuration to write
    * @param outputStream The stream in which the configuration should be written
    * @param blocker the blocking context which will be used to load the configuration.
    * @param options the config rendering options
    * @return The return action will save out the supplied configuration upon invocation
    */
  def blockingSaveConfigToStreamF[F[_], A](
      conf: A,
      outputStream: OutputStream,
      blocker: Blocker,
      options: ConfigRenderOptions = ConfigRenderOptions.defaults()
  )(implicit F: Sync[F], csf: ContextShift[F], writer: Derivation[ConfigWriter[A]]): F[Unit] =
    F.delay(writer.value.to(conf)).map { rawConf =>
      // HOCON requires UTF-8:
      // https://github.com/lightbend/config/blob/master/HOCON.md#unchanged-from-json
      StandardCharsets.UTF_8.encode(rawConf.render(options)).array
    } flatMap { bytes =>
      blocker.delay {
        outputStream.write(bytes)
        outputStream.flush()
      }
    }

  /** Loads `files` in order, allowing values in later files to backstop missing values from prior, and converts them into a `A`.
    *
    * This is a convenience method which enables having default configuration which backstops local configuration.
    *
    * Note: If an element of `files` references a file which doesn't exist or can't be read, it will silently be ignored.
    *
    * @param files Files ordered in decreasing priority containing part or all of a `A`. Must not be empty.
    */
  @deprecated("Construct a custom `ConfigSource` pipeline instead", "0.12.0")
  def loadConfigFromFilesF[F[_], A](
      files: NonEmptyList[Path]
  )(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] =
    loadF[F, A](
      ConfigSource.default(
        files
          .map(ConfigSource.file(_).optional)
          .foldLeft(ConfigSource.empty)(_.withFallback(_))
      )
    )
}
