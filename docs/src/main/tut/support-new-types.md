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

We can split the types that can be supported in two big groups, depending on what part of the configuration
is needed to read them:

1. **Simple types** whose values can be converter from a **single value** in the configuration. A good
example is `Int`, which has a `ConfigReader` instance that just convert a string value into a `Int`
when possible. See [Add support for simple types](add-support-for-simple-types.md) for this group.
2. **Complex types** that require access to an entire **sub-tree of the configuration** to be read.
An example of this group are case classes. See [Add support for complex types](add-support-for-complex-types.md)
for this group.