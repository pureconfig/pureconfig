package pureconfig.module.zioconfig

import zio.Config

import pureconfig.error.FailureReason

final case class ZioConfigReadError[A](re: Config.Error) extends FailureReason {
  override def description: String = s"ZioConfigReadError: $re"
}
