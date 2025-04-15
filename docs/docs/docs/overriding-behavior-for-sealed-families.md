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

```scala mdoc:silent
import pureconfig._
import pureconfig.generic.auto._

sealed trait AnimalConf
case class DogConf(age: Int) extends AnimalConf
case class BirdConf(canFly: Boolean) extends AnimalConf
```

This will load a `DogConf` instance:

```scala mdoc
ConfigSource.string("{ type: dog-conf, age: 4 }").load[AnimalConf]
```

For sealed families, PureConfig provides a way to customize the conversion
without replacing the default `ConfigReader`. By putting in scope an instance
of `CoproductHint` for that sealed family, we can customize how the
disambiguation is made. For example, if `type` clashes with one of the fields
of a case class option, we can use another field.

First, define a `CoproductHint` in implicit scope:

```scala mdoc:silent:nest
import pureconfig.generic.FieldCoproductHint

implicit val animalConfHint: FieldCoproductHint[AnimalConf] = new FieldCoproductHint[AnimalConf]("kind")
```

Then load the config:

```scala mdoc
ConfigSource.string("{ kind: dog-conf, age: 4 }").load[AnimalConf]
```

`FieldCoproductHint` can also be adapted to write class names in a different
way. First, define a new `FieldCoproductHint` in implicit scope:

```scala mdoc:nest:silent
implicit val animalConfHint: FieldCoproductHint[AnimalConf] = new FieldCoproductHint[AnimalConf]("type") {
  override def fieldValue(name: String) = name.dropRight("Conf".length)
}
```

Then load the config:

```scala mdoc
ConfigSource.string("{ type: Bird, can-fly: true }").load[AnimalConf]
```

If you encode enumerations using sealed traits of case objects, you can use the
`deriveEnumerationReader` method from the `pureconfig.generic.semiauto` package
to derive `ConfigReader` instances for your sealed trait.

```scala mdoc:reset:silent
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.generic.semiauto._

sealed trait Season
case object Spring extends Season
case object Summer extends Season
case object Autumn extends Season
case object Winter extends Season

implicit val seasonConvert: ConfigReader[Season] = deriveEnumerationReader[Season]

case class MyConf(list: List[Season])
```

We can load seasons by specifying them by class name:

```scala mdoc
ConfigSource.string("{ list: [spring, summer, autumn, winter] }").load[MyConf]
```

By default, enumerations will be encoded as strings with the `kebab-case`
representation of the class name, but that behavior can be overridden by
specifying a different transformation function.

```scala mdoc:silent
sealed trait Color
case object RainyBlue extends Color
case object SunnyYellow extends Color

implicit val colorReader: ConfigReader[Color] = deriveEnumerationReader[Color](ConfigFieldMapping(PascalCase, SnakeCase))

case class ColorList(colors: List[Color])
```

```scala mdoc
ConfigSource.string("{ colors: [rainy_blue, sunny_yellow] }").load[ColorList]
```
