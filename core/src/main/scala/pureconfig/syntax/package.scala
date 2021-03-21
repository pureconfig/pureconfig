package pureconfig

import scala.reflect.ClassTag

import com.typesafe.config.{Config => TypesafeConfig, ConfigValue}

import pureconfig.error.ConfigReaderException

package object syntax {
  implicit class AnyWriterOps[A](val any: A) extends AnyVal {
    def toConfig(implicit writer: Derivation[ConfigWriter[A]]): ConfigValue = writer.value.to(any)
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
    def to[A](implicit reader: Derivation[ConfigReader[A]]): ConfigReader.Result[A] = reader.value.from(conf)
    def toOrThrow[A](implicit reader: Derivation[ConfigReader[A]], cl: ClassTag[A]): A =
      getResultOrThrow(reader.value.from(conf))(cl)
  }

  implicit class ConfigCursorReaderOps(val cur: ConfigCursor) extends AnyVal {
    def to[A](implicit reader: Derivation[ConfigReader[A]]): ConfigReader.Result[A] = reader.value.from(cur)
    def toOrThrow[A](implicit reader: Derivation[ConfigReader[A]], cl: ClassTag[A]): A =
      getResultOrThrow(reader.value.from(cur))(cl)
  }

  implicit class ConfigReaderOps(val conf: TypesafeConfig) extends AnyVal {
    def to[A](implicit reader: Derivation[ConfigReader[A]]): ConfigReader.Result[A] = conf.root().to[A]
    def toOrThrow[A](implicit reader: Derivation[ConfigReader[A]], cl: ClassTag[A]): A =
      getResultOrThrow(conf.root().to[A])(cl)
  }
}
