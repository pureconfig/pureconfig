## Support new types

Not all types are supported automatically by PureConfig. For instance, classes that are not case classes are not
supported out-of-the-box:

```tut:silent
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.syntax._

class MyInt(var value: Int) {
  override def toString: String = s"MyInt($value)"
}

case class Conf(n: MyInt)

val conf = parseString(s"""{ n: 1 }""")
```

In order to read an instance of a given type `T` from a config, PureConfig needs to have in scope in implicit instance
of `ConfigReader[T]` for that type. This won't compile because there's no `ConfigReader` instance for `MyInt`:

```tut:book:fail
loadConfig[Conf](conf)
```

PureConfig can be extended to support those types. To do so, an instance for the `ConfigReader` type class must be
provided.

First, define a `ConfigReader` instance in implicit scope:

```tut:silent
import pureconfig.ConvertHelpers._

implicit val myIntReader = ConfigReader.fromString[MyInt](catchReadError(s => new MyInt(s.toInt)))
```

Then load the config:

```tut:book
loadConfig[Conf](conf)
```

In some situations, you may want to write an object to a config. For unsupported types, you'll see the following error:

```tut:book:fail
Conf(new MyInt(3)).toConfig
```

Just as with reading, you'll have to provide an instance of `ConfigWriter` for the type of the object you want to write:

```tut:book:silent
implicit val myIntWriter = ConfigWriter.toString[MyInt](n => n.value.toString)
```

And then:

```tut:book
Conf(new MyInt(3)).toConfig
```

If you want to define both operations, the easier way to add full support for a class is by creating a `ConfigConvert`:

```tut:book:silent
implicit val myIntConvert = ConfigConvert.viaString[MyInt](
  catchReadError(s => new MyInt(s.toInt)),
  n => n.value.toString)
```

A `ConfigConvert` is both an instance of `ConfigReader` and an instance of `ConfigWriter`, so it can be used everywhere
one of them is required.
