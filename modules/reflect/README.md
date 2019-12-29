# Reflect module for PureConfig

This module simplifies the [manual][1] approach for `ConfigReader` derivation by using a reflection based approach similar to
[spray-json][2]. In this mode reflection is used only for discovering the case class properties at derivation time and post derivation
there is no "reflection tx" to be paid.

This module is an alternative to `pureconfig-generic` and only supports product based derivation. Instead of relying on Shapeless 
for generic derivation it uses reflection to discover properties.

## Add pureconfig-reflect to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-reflect" % "0.12.3"
``` 

## Example

```scala
package example

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

Now define the `ConfigReader`. It can be either placed in companion object for `MyClass` or in package object

```scala
import pureconfig._
import pureconfig.module.reflect.ReflectConfigReaders._

package object example {
  implicit val myClassConfigReader: ConfigReader[MyClass] = configReader6(MyClass)
}
```

We are now able to read configs to case classes

```scala
source.load[MyClass]
```

[1]: https://pureconfig.github.io/docs/non-automatic-derivation.html#manual
[2]: https://github.com/spray/spray-json#providing-jsonformats-for-case-classes