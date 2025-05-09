# Magnolia module for PureConfig

This module is an alternative to `pureconfig-generic` for deriving `ConfigReader` and `ConfigWriter` instances for case
classes, sealed traits, value classes and tuples. Instead of relying on Shapeless for generic derivation it uses
[Magnolia](https://propensive.com/opensource/magnolia), which should make compilation times faster. It supports
configuration using the same [product](https://pureconfig.github.io/docs/overriding-behavior-for-case-classes.html) and
[coproduct hints](https://pureconfig.github.io/docs/overriding-behavior-for-sealed-families.html) as
`pureconfig-generic`.

## Add pureconfig-magnolia to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-magnolia" % "0.17.9"
```

## Example

The only thing needed to use Magnolia-based derivation is to replace the `pureconfig.generic.auto._` import with
`pureconfig.module.magnolia.auto.reader._`:

```scala
import pureconfig._
import pureconfig.module.magnolia.auto.reader._

sealed trait MyAdt
case class AdtA(a: String) extends MyAdt
case class AdtB(b: Int) extends MyAdt
final case class Port(value: Int) extends AnyVal
case class MyClass(
  boolean: Boolean,
  port: Port,
  adt: MyAdt,
  list: List[Double],
  map: Map[String, String],
  option: Option[String])

val source = ConfigSource.string("""
  boolean = true
  port = 8080
  adt {
    type = "adt-b"
    b = 1
  }
  list = ["1", "20%"]
  map {
    key = "value"
  }
""")
```

We are now able to read configs to case classes and sealed families:

```scala
source.load[MyClass]
// res1: ConfigReader.Result[MyClass] = Right(
//   value = MyClass(
//     boolean = true,
//     port = Port(value = 8080),
//     adt = AdtB(b = 1),
//     list = List(1.0, 0.2),
//     map = Map("key" -> "value"),
//     option = None
//   )
// )
```

You can do with this module most of the things allowed by the default generic derivation, like creating hints for
certain types, writing configs and using semi-auto derivation. Please refer to the
[main documentation](https://pureconfig.github.io/docs) for more information on PureConfig features.

## Differences in Behavior

- Value classes that are not case classes are not supported (other value classes have the same reading behavior).
