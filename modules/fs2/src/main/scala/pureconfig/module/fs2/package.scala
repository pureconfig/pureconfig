package pureconfig.module

import java.nio.charset.StandardCharsets.UTF_8

import scala.reflect.ClassTag

import _root_.fs2.{Stream, text}
import cats.effect.Sync
import cats.implicits._
import com.typesafe.config.ConfigRenderOptions

import pureconfig.backend.ConfigFactoryWrapper
import pureconfig.error.ConfigReaderException
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}

package object fs2 {

  /** Load a configuration of type `A` from the given byte stream.
    *
    * @param configStream
    *   a stream of bytes representing the contents of a configuration file
    * @return
    *   The returned action will complete with `A` if it is possible to create an instance of type `A` from the
    *   configuration stream, or fail with a ConfigReaderException which in turn contains details on why it isn't
    *   possible It can also raise any exception that the stream can raise.
    */
  def streamConfig[F[_], A](
      configStream: Stream[F, Byte]
  )(implicit F: Sync[F], reader: ConfigReader[A], ct: ClassTag[A]): F[A] = {
    for {
      bytes <- configStream.compile.to(Array)
      string = new String(bytes, UTF_8)
      configOrError <- F.delay(ConfigFactoryWrapper.parseString(string))
      config <- F.fromEither(configOrError.leftMap(ConfigReaderException[A]))
      aOrError <- F.delay(ConfigSource.fromConfig(config).load[A])
      a <- F.fromEither(aOrError.leftMap(ConfigReaderException[A]))
    } yield a
  }

  /** Writes the configuration to a fs2 byte stream
    *
    * @param config
    *   The configuration to write
    * @param options
    *   the config rendering options
    * @return
    *   the configuration as a stream of utf-8 bytes
    */
  def saveConfigToStream[F[_], A](config: A, options: ConfigRenderOptions = ConfigRenderOptions.defaults())(implicit
      writer: ConfigWriter[A]
  ): Stream[F, Byte] = {

    val asString = writer.to(config).render(options)
    Stream.emit(asString).through(text.utf8.encode)
  }
}
