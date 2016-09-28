package pureconfig.error

final case class KeyNotFoundException(key: String)
  extends IllegalArgumentException(s"Could not find the key $key")
