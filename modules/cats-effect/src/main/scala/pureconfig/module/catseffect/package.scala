package pureconfig.module

import java.io.OutputStream
import java.nio.file.Path

import scala.language.higherKinds
import scala.reflect.ClassTag

import cats.effect.{ Blocker, ContextShift, Sync }
import cats.implicits._
import com.typesafe.config.ConfigRenderOptions
import pureconfig._
import pureconfig.error.ConfigReaderException

package object catseffect {

  private[catseffect] type ConfReader[A] = Derivation[ConfigReader[A]]
  private[catseffect] type ConfWriter[A] = Derivation[ConfigWriter[A]]

  def loadF[F[_]: Sync: ContextShift, A: ConfReader: ClassTag](cs: ConfigSource, blocker: Blocker): F[A] =
    blocker.delay(cs.load[A].leftMap[Throwable](ConfigReaderException[A])).rethrow

  /**
   * Load a configuration of type `A` from the standard configuration files
   *
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration files, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  def loadConfigF[F[_]: Sync: ContextShift, A: ConfReader: ClassTag](blocker: Blocker): F[A] =
    loadF[F, A](ConfigSource.default, blocker)

  /**
   * Save the given configuration into a property file
   *
   * @param conf The configuration to save
   * @param outputPath Where to write the configuration
   * @param overrideOutputPath Override the path if it already exists
   * @param options the config rendering options
   * @return The return action will save out the supplied configuration upon invocation
   */
  def saveConfigAsPropertyFileF[F[_]: Sync: ContextShift, A: ConfWriter](
    conf: A,
    outputPath: Path,
    blocker: Blocker,
    overrideOutputPath: Boolean = false,
    options: ConfigRenderOptions = ConfigRenderOptions.defaults()): F[Unit] =
    blocker.delay(pureconfig.saveConfigAsPropertyFile(conf, outputPath, overrideOutputPath, options))

  /**
   * Writes the configuration to the output stream and closes the stream
   *
   * @param conf The configuration to write
   * @param outputStream The stream in which the configuration should be written
   * @param options the config rendering options
   * @return The return action will save out the supplied configuration upon invocation
   */
  def saveConfigToStreamF[F[_]: Sync: ContextShift, A: ConfWriter](
    conf: A,
    outputStream: OutputStream,
    blocker: Blocker,
    options: ConfigRenderOptions = ConfigRenderOptions.defaults()): F[Unit] =
    blocker.delay(pureconfig.saveConfigToStream(conf, outputStream, options))
}
