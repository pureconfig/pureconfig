package pureconfig.module

import com.typesafe.config.Config
import zio.config.typesafe._
import zio.{Config => ZioConfig, ConfigProvider, IO, Runtime, Unsafe}

import pureconfig.ConfigReader

package object zioconfig {
  implicit def zioConfigReader[A](implicit cd: ZioConfig[A]): ConfigReader[A] =
    ConfigReader[Config].emap(c =>
      runUnsafe { ConfigProvider.fromTypesafeConfig(c).load(cd) }.left.map(ZioConfigReadError.apply)
    )

  private def runUnsafe[E, A](expr: IO[E, A]): Either[E, A] = {
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(expr.either).getOrThrow()
    }
  }
}
