package pureconfig

import com.typesafe.config.{ ConfigValue, Config => TypesafeConfig }
import pureconfig.error.{ ConfigReaderException, ConfigReaderFailures }

import scala.reflect.ClassTag

package object syntax {
  implicit class PimpedAny[T](val any: T) extends AnyVal {
    def toConfig(implicit writer: ConfigWriter[T]): ConfigValue = writer.to(any)
  }

  private def getResultOrThrow[Config](failuresOrResult: Either[ConfigReaderFailures, Config])(implicit ct: ClassTag[Config]): Config = {
    failuresOrResult match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  implicit class PimpedConfigValue(val conf: ConfigValue) extends AnyVal {
    def to[T](implicit reader: ConfigReader[T]): Either[ConfigReaderFailures, T] = reader.from(conf)
    def toOrThrow[T](implicit reader: ConfigReader[T], cl: ClassTag[T]): T = getResultOrThrow(reader.from(conf))(cl)
  }

  implicit class PimpedConfig(val conf: TypesafeConfig) extends AnyVal {
    def to[T: ConfigReader]: Either[ConfigReaderFailures, T] = conf.root().to[T]
    def toOrThrow[T](implicit reader: ConfigReader[T], cl: ClassTag[T]): T = getResultOrThrow(conf.root().to[T])(cl)
  }
}
