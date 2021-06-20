package pureconfig.module

import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.data.EitherT
import cats.effect.{Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import com.typesafe.config.ConfigRenderOptions

import pureconfig._
import pureconfig.error.ConfigReaderException

package object catseffect2 {

  /** Load a configuration of type `A` from a config source
    *
    * @param cs
    *   the config source from where the configuration will be loaded
    * @param blocker
    *   the blocking context which will be used to load the configuration.
    * @return
    *   The returned action will complete with `A` if it is possible to create an instance of type `A` from the
    *   configuration source, or fail with a ConfigReaderException which in turn contains details on why it isn't
    *   possible
    */
  def loadF[F[_]: Sync: ContextShift, A](
      cs: ConfigSource,
      blocker: Blocker
  )(implicit reader: ConfigReader[A], ct: ClassTag[A]): F[A] =
    EitherT(blocker.delay(cs.cursor()))
      .subflatMap(reader.from)
      .leftMap(ConfigReaderException[A])
      .rethrowT

  /** Load a configuration of type `A` from the standard configuration files
    *
    * @param blocker
    *   the blocking context which will be used to load the configuration.
    * @return
    *   The returned action will complete with `A` if it is possible to create an instance of type `A` from the
    *   configuration files, or fail with a ConfigReaderException which in turn contains details on why it isn't
    *   possible
    */
  def loadConfigF[F[_]: Sync: ContextShift, A: ConfigReader](blocker: Blocker)(implicit ct: ClassTag[A]): F[A] =
    loadF(ConfigSource.default, blocker)

  /** Save the given configuration into a property file
    *
    * @param conf
    *   The configuration to save
    * @param outputPath
    *   Where to write the configuration
    * @param blocker
    *   the blocking context which will be used to load the configuration.
    * @param overrideOutputPath
    *   Override the path if it already exists
    * @param options
    *   the config rendering options
    * @return
    *   The return action will save out the supplied configuration upon invocation
    */
  def blockingSaveConfigAsPropertyFileF[F[_]: ContextShift, A: ConfigWriter](
      conf: A,
      outputPath: Path,
      blocker: Blocker,
      overrideOutputPath: Boolean = false,
      options: ConfigRenderOptions = ConfigRenderOptions.defaults()
  )(implicit F: Sync[F]): F[Unit] = {
    val fileAlreadyExists =
      F.raiseError(
        new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it already exists")
      )

    val fileIsDirectory =
      F.raiseError(
        new IllegalArgumentException(s"Cannot save configuration in file '$outputPath' because it is a directory")
      )

    val check =
      F.defer {
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
    * @param conf
    *   The configuration to write
    * @param outputStream
    *   The stream in which the configuration should be written
    * @param blocker
    *   the blocking context which will be used to load the configuration.
    * @param options
    *   the config rendering options
    * @return
    *   The return action will save out the supplied configuration upon invocation
    */
  def blockingSaveConfigToStreamF[F[_]: ContextShift, A](
      conf: A,
      outputStream: OutputStream,
      blocker: Blocker,
      options: ConfigRenderOptions = ConfigRenderOptions.defaults()
  )(implicit F: Sync[F], writer: ConfigWriter[A]): F[Unit] =
    F.delay(writer.to(conf)).map { rawConf =>
      // HOCON requires UTF-8:
      // https://github.com/lightbend/config/blob/master/HOCON.md#unchanged-from-json
      StandardCharsets.UTF_8.encode(rawConf.render(options)).array
    } flatMap { bytes =>
      blocker.delay {
        outputStream.write(bytes)
        outputStream.flush()
      }
    }

}
