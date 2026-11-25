package pureconfig

sealed trait AnimalConfig
case class DogConfig(age: Int) extends AnimalConfig
case class CatConfig(age: Int) extends AnimalConfig
case class BirdConfig(canFly: Boolean) extends AnimalConfig

sealed trait WildAnimalConfig extends AnimalConfig
case class LionConfig(speed: Int) extends WildAnimalConfig
case class TigerConfig(strength: Int) extends WildAnimalConfig
