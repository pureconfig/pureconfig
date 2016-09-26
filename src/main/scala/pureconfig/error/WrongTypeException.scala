package pureconfig.error

case class WrongTypeException(typ: String) extends Exception
case class WrongTypeForKeyException(typ: String, key: String)
  extends IllegalArgumentException(s"Cannot convert key $key from type $typ")
