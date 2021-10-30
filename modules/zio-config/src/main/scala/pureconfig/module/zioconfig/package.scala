package pureconfig.module

import com.typesafe.config.Config
import zio.config._
import zio.config.typesafe._

import pureconfig.ConfigConvert

package object zioconfig {
  implicit def zioConfigConvert[A](implicit cd: ConfigDescriptor[A]): ConfigConvert[A] =
    ConfigConvert[Config].xemap(
      c =>
        TypesafeConfigSource
          .fromConfig(c)
          .flatMap(cs => read(cd.from(cs)))
          .left
          .map(ZioConfigReadError.apply),
      // `zio-config` write should typically not fail but is allowed to fail
      // an exception will be throw on write errors
      _.toHocon(cd)
        .map(_.toConfig)
        .fold(s => throw new Exception(s"ZioConfigWriteException: $s"), identity)
    )
}
