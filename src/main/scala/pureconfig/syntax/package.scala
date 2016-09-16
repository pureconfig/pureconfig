package pureconfig

import com.typesafe.config.{ Config => TypesafeConfig, ConfigValue }
import scala.util.Try

package object syntax {
  import ConfigConvert._

  implicit class PimpedAny[T](val any: T) extends AnyVal {
    def toConfig(implicit configConvert: ConfigConvert[T]): ConfigValue = configConvert.to(any)
  }

  implicit class PimpedConfigValue(val conf: ConfigValue) extends AnyVal {
    def to[T](implicit configConvert: ConfigConvert[T]): Try[T] = configConvert.from(conf)
  }

  implicit class PimpedConfig(val conf: TypesafeConfig) extends AnyVal {
    def to[T: ConfigConvert]: Try[T] = conf.root().to[T]
  }
}
