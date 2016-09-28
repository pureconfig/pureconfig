package pureconfig.contrib.implicits

import scalaz._
import scalaz.Validation.FlatMap._
import argonaut._, Argonaut._
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, ConfigValue}
import pureconfig.ConfigConvert

import scala.reflect.ClassTag
import scala.util.Try

object `package` {
  implicit def configConvertFromArgonaut[T: EncodeJson : DecodeJson](implicit ct: ClassTag[T]): ConfigConvert[T] = new ConfigConvert[T] {
    def from(config: ConfigValue): Try[T] = Try(
      config.render(ConfigRenderOptions.concise())
      .parse.leftMap(str => NonEmptyList(s"Cannot produce json from config: $str")).validation
      .flatMap(_.as[T].toDisjunction.leftMap{ case (str, ch) => NonEmptyList(s"Cannot parse as ${ct.runtimeClass.getName}: $str. Cursor history: $ch") }.validation)
      .valueOr(nel => throw new Exception(s"Error occurred: $nel"))
    )
    def to(t: T): ConfigValue = ConfigFactory.parseString(EncodeJson.of[T].encode(t).nospaces).root()
  }
}
