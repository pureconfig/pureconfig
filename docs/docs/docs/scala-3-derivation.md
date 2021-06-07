---
layout: docs
title: Scala 3 Derivation
---

## {{page.title}}

Scala 3 introduced native support for
[type class derivation](https://dotty.epfl.ch/docs/reference/contextual/derivation.html) - a way to automatically
generate type class instances for enums, enum cases, case classes, case objects and sealed traits that only have the
aforementioned types as children. Instances are generated on demand by adding a `derives` clause on data types.

PureConfig has some limited support for derivation of `ConfigReader` instances. In order to enable it, the relevant
extensions must be imported:

```scala
import pureconfig._
import pureconfig.generic.derivation.default._
```

After that, you can derive `ConfigReader` instaces for your config class using a `derives` clause:

```scala
sealed trait AnimalConf derives ConfigReader
case class DogConf(age: Int) extends AnimalConf
case class BirdConf(canFly: Boolean) extends AnimalConf

ConfigSource.string("{ type: dog-conf, age: 4 }").load[AnimalConf]
// val res0: pureconfig.ConfigReader.Result[AnimalConf] = Right(DogConf(4))
```

Readers for enumerations of objects can also be derived by using `EnumConfigReader` instead:

```scala
import pureconfig.generic.derivation.EnumConfigReader

enum Season derives EnumConfigReader {
  case Spring, Summer, Autumn, Winter
}

case class MyConf(list: List[Season]) derives ConfigReader

ConfigSource.string("{ list: [spring, summer, autumn, winter] }").load[MyConf]
// val res1: pureconfig.ConfigReader.Result[MyConf] = Right(MyConf(List(Spring, Summer, Autumn, Winter)))
```

### Limitations

There is currently no way to customize case class and sealed trait derivation - any `ProductHint` and `CoproductHint`
instances in scope are ignored. 

`ConfigWriter` derivation is not supported at the moment.

Overall, consider this to be in alpha/beta stage. This implementation is not as mature as the Shapeless or
Magnolia-based derivation libaries available in Scala 2 yet so please report any bugs that you find!
