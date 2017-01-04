package pureconfig.error

case class CollidingKeysException(key: String, existingValue: String)
  extends IllegalArgumentException(s"The coproduct hint key '$key' collides with existing field '$existingValue'")
