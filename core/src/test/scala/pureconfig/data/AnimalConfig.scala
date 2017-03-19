package pureconfig.data

sealed trait AnimalConfig
case class DogConfig(age: Int) extends AnimalConfig
case class CatConfig(age: Int) extends AnimalConfig
case class BirdConfig(canFly: Boolean) extends AnimalConfig
