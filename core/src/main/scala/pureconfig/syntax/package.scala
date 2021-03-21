package pureconfig

import com.typesafe.config.{ConfigValue, Config => TypesafeConfig}
import pureconfig.error.ConfigReaderException

import scala.reflect.ClassTag

package object syntax {
  implicit class AnyWriterOps[A](val any: A) extends AnyVal {
    def toConfig(implicit writer: ConfigWriter[A]): ConfigValue = writer.to(any)
  }

  private def getResultOrThrow[Config](
      failuresOrResult: ConfigReader.Result[Config]
  )(implicit ct: ClassTag[Config]): Config = {
    failuresOrResult match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  implicit class ConfigValueReaderOps(val conf: ConfigValue) extends AnyVal {
    def to[A](implicit reader: ConfigReader[A]): ConfigReader.Result[A] = reader.from(conf)
    def toOrThrow[A](implicit reader: ConfigReader[A], cl: ClassTag[A]): A =
      getResultOrThrow(reader.from(conf))(cl)
  }

  implicit class ConfigCursorReaderOps(val cur: ConfigCursor) extends AnyVal {
    def to[A](implicit reader: ConfigReader[A]): ConfigReader.Result[A] = reader.from(cur)
    def toOrThrow[A](implicit reader: ConfigReader[A], cl: ClassTag[A]): A =
      getResultOrThrow(reader.from(cur))(cl)
  }

  implicit class ConfigReaderOps(val conf: TypesafeConfig) extends AnyVal {
    def to[A](implicit reader: ConfigReader[A]): ConfigReader.Result[A] = conf.root().to[A]
    def toOrThrow[A](implicit reader: ConfigReader[A], cl: ClassTag[A]): A =
      getResultOrThrow(conf.root().to[A])(cl)
  }
}
