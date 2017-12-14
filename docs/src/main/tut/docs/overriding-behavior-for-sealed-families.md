---
layout: docs
title: Sealed Families
---

## Sealed Families

In order for PureConfig to disambiguate between different options of a sealed
family of case classes, it must read and write additional information in
configurations. By default it uses the additional field `type`, encoding the
concrete class represented in the configuration.

Given an `AnimalConf` sealed trait:

```tut:silent
import com.typesafe.config.ConfigFactory
import pureconfig._

sealed trait AnimalConf
case class DogConf(age: Int) extends AnimalConf
case class BirdConf(canFly: Boolean) extends AnimalConf
```

This will load a `DogConf` instance:

```tut:book
loadConfig[AnimalConf](ConfigFactory.parseString("{ type: dogconf, age: 4 }"))
```

For sealed families, PureConfig provides a way to customize the conversion
without replacing the default `ConfigReader`. By putting in scope an instance
of `CoproductHint` for that sealed family, we can customize how the
disambiguation is made. For example, if `type` clashes with one of the fields
of a case class option, we can use another field.

First, define a `CoproductHint` in implicit scope:

```tut:silent
implicit val animalConfHint = new FieldCoproductHint[AnimalConf]("kind")
```

Then load the config:

```tut:book
loadConfig[AnimalConf](ConfigFactory.parseString("{ kind: dogconf, age: 4 }"))
```

`FieldCoproductHint` can also be adapted to write class names in a different
way. First, define a new `FieldCoproductHint` in implicit scope:

```tut:silent
implicit val animalConfHint = new FieldCoproductHint[AnimalConf]("type") {
  override def fieldValue(name: String) = name.dropRight("Conf".length)
}
```

Then load the config:

```tut:book
loadConfig[AnimalConf](ConfigFactory.parseString("{ type: Bird, can-fly: true }"))
```

With a `CoproductHint` you can even opt not to use any extra field at all. If you encode enumerations using sealed
traits, you can just write the lowercase name of the class by using an `EnumCoproductHint`. For example, if we create
an enumeration for seasons:

```tut:silent
sealed trait Season
case object Spring extends Season
case object Summer extends Season
case object Autumn extends Season
case object Winter extends Season

implicit val seasonHint = new EnumCoproductHint[Season]

case class MyConf(list: List[Season])
```

We can load seasons by specifying them by class name:

```tut:book
loadConfig[MyConf](ConfigFactory.parseString("{ list: [spring, summer, autumn, winter] }"))
```
