package pureconfig.module.zioconfig

import zio.config.ReadError

import pureconfig.error.FailureReason

final case class ZioConfigReadError[A](re: ReadError[A]) extends FailureReason {
  override def description: String = s"ZioConfigReadError: $re"
}
