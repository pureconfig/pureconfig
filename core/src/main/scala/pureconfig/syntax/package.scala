package pureconfig

import com.typesafe.config.{ConfigValue, Config => TypesafeConfig}
import pureconfig.error.{ConfigReaderException, ConfigReaderFailures}

import scala.reflect.ClassTag

package object syntax {
  implicit class PimpedAny[T](val any: T) extends AnyVal {
    def toConfig(implicit configConvert: ConfigConvert[T]): ConfigValue = configConvert.to(any)
  }

  private def getResultOrThrow[Config](failuresOrResult: Either[ConfigReaderFailures, Config])(implicit ct: ClassTag[Config]): Config = {
    failuresOrResult match {
      case Right(config) => config
      case Left(failures) => throw new ConfigReaderException[Config](failures)
    }
  }

  implicit class PimpedConfigValue(val conf: ConfigValue) extends AnyVal {
    def to[T](implicit configConvert: ConfigConvert[T]): Either[ConfigReaderFailures, T] = configConvert.from(conf)
    def toOrThrow[T](implicit configConvert: ConfigConvert[T], cl:ClassTag[T]): T = getResultOrThrow(configConvert.from(conf))(cl)
  }



  implicit class PimpedConfig(val conf: TypesafeConfig) extends AnyVal {
    def to[T: ConfigConvert]: Either[ConfigReaderFailures, T] = conf.root().to[T]
    def toOrThrow[T](implicit configConvert: ConfigConvert[T], cl:ClassTag[T]): T = getResultOrThrow(conf.root().to[T])(cl)
  }
}
