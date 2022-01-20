package pureconfig

import pureconfig.Secret.SecretBytes

import java.nio.charset.{Charset, StandardCharsets}

case class Secret[+T](value: T) extends AnyVal {

  def map[U](f: T => U): Secret[U] =
    Secret(f(value))

  override final def toString: String = Secret.placeHolder
}
object Secret extends SecretSyntax {

  type SecretBytes = Secret[Array[Byte]]

  val placeHolder: String = "** MASKED **"

  implicit def secretConfigReader[T: ConfigReader]: ConfigReader[Secret[T]] =
    ConfigReader[T].map(n => Secret(n))

  implicit def secretConfigWriter[T: ConfigWriter]: ConfigWriter[Secret[T]] =
    ConfigWriter[T].contramap(_.value)
}

sealed trait SecretSyntax {

  implicit class SecretBytesOps(sb: SecretBytes) {
    def stringValue(cs: Charset = StandardCharsets.UTF_8): String =
      new String(sb.value, cs)
  }
}
