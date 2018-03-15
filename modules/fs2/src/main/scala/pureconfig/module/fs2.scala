package pureconfig.module

import java.io._

import pureconfig.{ ConfigReader, ConfigWriter, Derivation }

import scala.language.higherKinds
import cats.effect.{ Effect, Sync }
import com.typesafe.config.ConfigFactory
import _root_.fs2.{ async, io, Stream, text }
import pureconfig.error.ConfigReaderException
import cats.implicits._

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

package object fs2 {

  private def parseStream[F[_], A](inputStream: InputStream)(implicit F: Sync[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A]): F[A] = {
    val streamReader = new InputStreamReader(inputStream)
    val config = F.delay { ConfigFactory.parseReader(streamReader) }
    config.flatMap { c =>
      val loadedConfig = pureconfig.loadConfig(c).leftMap(ConfigReaderException[A])
      F.fromEither(loadedConfig)
    }
  }

  private def pipedStreams[F[_]](implicit F: Sync[F]): F[(InputStream, OutputStream)] = F.delay {
    val output = new PipedOutputStream()
    val input = new PipedInputStream(output)
    (input, output)
  }

  /**
   * Load a configuration of type `A` from the given byte stream.
   *
   * @param configStream a stream of bytes representing the contents of a configuration file
   * @return The returned action will complete with `A` if it is possible to create an instance of type
   *         `A` from the configuration stream, or fail with a ConfigReaderException which in turn contains
   *         details on why it isn't possible
   */
  def streamConfig[F[_], A](configStream: Stream[F, Byte])(implicit F: Effect[F], reader: Derivation[ConfigReader[A]], ct: ClassTag[A], ec: ExecutionContext): F[A] = {
    pipedStreams.flatMap {
      case (in, out) =>
        val outputSink = io.writeOutputStream[F](out.pure[F])
        val sunkStream = configStream.to(outputSink)

        val parseConfig = parseStream(in)

        async.fork(sunkStream.compile.drain) >> parseConfig
    }
  }

  /**
   * Writes the configuration to a fs2 byte stream
   *
   * @param config The configuration to write
   * @return the configuration as a stream of utf-8 bytes
   */
  def saveConfigToStream[F[_], A](config: A)(implicit writer: Derivation[ConfigWriter[A]]): Stream[F, Byte] = {
    val asString = writer.value.to(config).render()
    Stream.emit(asString).through(text.utf8Encode)
  }

}
