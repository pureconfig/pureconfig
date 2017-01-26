package pureconfig.error

final case class UnknownKeyException(key: String)
  extends IllegalArgumentException(s"Found unknown key $key")
