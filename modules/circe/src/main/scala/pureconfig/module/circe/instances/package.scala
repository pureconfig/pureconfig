package pureconfig.module.circe

import io.circe.{Decoder, Encoder}

import pureconfig.Secret

package object instances {

  private val encodeSecretAny: Encoder[Secret[Any]] =
    Encoder.encodeString.contramap(_ => Secret.placeHolder)

  implicit def encodeSecret[T]: Encoder[Secret[T]] =
    encodeSecretAny.contramap(identity)

  implicit def decodeSecret[T: Decoder]: Decoder[Secret[T]] =
    Decoder[T].map(Secret(_))
}
