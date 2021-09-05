package pureconfig.module

import com.typesafe.config.{Config, ConfigFactory}
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
      //`zio-config` write should not fail but is allowed to fail
      //defaulting write failures to empty `Config` so it fits `pureconfig` writer
      _.toHocon(cd).map(_.toConfig).getOrElse(ConfigFactory.empty)
    )
}
