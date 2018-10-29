package pureconfig

import com.typesafe.config.{ ConfigValue, Config => TypesafeConfig }
import pureconfig.error.{ ConfigReaderException, ConfigReaderFailures }

import scala.reflect.ClassTag

package object syntax {
  implicit class AnyWriterOps[T](val any: T) extends AnyVal {
    def toConfig(implicit writer: Derivation[ConfigWriter[T]]): ConfigValue = writer.value.to(any)
  }

  private def getResultOrThrow[Config](failuresOrResult: Either[ConfigReaderFailures, Config])(implicit ct: ClassTag[Config]): Config = {
    failuresOrResult match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  implicit class ConfigValueReaderOps(val conf: ConfigValue) extends AnyVal {
    def to[T](implicit reader: Derivation[ConfigReader[T]]): Either[ConfigReaderFailures, T] = reader.value.from(conf)
    def toOrThrow[T](implicit reader: Derivation[ConfigReader[T]], cl: ClassTag[T]): T = getResultOrThrow(reader.value.from(conf))(cl)
  }

  implicit class ConfigCursorReaderOps(val cur: ConfigCursor) extends AnyVal {
    def to[T](implicit reader: Derivation[ConfigReader[T]]): Either[ConfigReaderFailures, T] = reader.value.from(cur)
    def toOrThrow[T](implicit reader: Derivation[ConfigReader[T]], cl: ClassTag[T]): T = getResultOrThrow(reader.value.from(cur))(cl)
  }

  implicit class ConfigReaderOps(val conf: TypesafeConfig) extends AnyVal {
    def to[T](implicit reader: Derivation[ConfigReader[T]]): Either[ConfigReaderFailures, T] = conf.root().to[T]
    def toOrThrow[T](implicit reader: Derivation[ConfigReader[T]], cl: ClassTag[T]): T = getResultOrThrow(conf.root().to[T])(cl)
  }
}
