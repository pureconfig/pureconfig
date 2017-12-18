---
layout: docs
title: Supporting New Types
---

## {{page.title}}

Not all types are supported automatically by PureConfig. For instance, classes that are not case classes are not
supported out-of-the-box:

```tut:silent
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.syntax._

class MyInt(var value: Int) {
  override def toString: String = s"MyInt($value)"
}

case class Conf(n: MyInt)
```

In order to read an instance of a given type `T` from a config, PureConfig needs to have in scope in implicit instance
of `ConfigReader[T]`. This won't compile because there's no `ConfigReader` instance for `MyInt`:

```tut:book:fail
loadConfig[Conf](ConfigFactory.parseString("{ n: 1 }"))
```

PureConfig can be extended to support those types. To do so, an instance for the `ConfigReader` type class must be
provided. There are three main ways to build such an instance:

- Modify an existing instance for another type by using one of the `ConfigReader` combinators;
- Use one of the `ConfigReader` convenience factory methods;
- Create a new implementation of the `ConfigReader` interface from scratch.

For the `MyInt` type above, we could create a `ConfigReader[MyInt]` by mapping the result of `ConfigReader[Int]` like
this:

```tut:book:silent
implicit val myIntReader = ConfigReader[Int].map(n => new MyInt(n))
```

Note that the `ConfigReader[Int]` expression "summons" an existing implicit instance, being syntatic sugar for `implicitly[ConfigReader[Int]]`. This is usually the easiest way to create a `ConfigReader` for simple types. See
[Combinators](combinators.html) for more examples.

As an example for second approach, we could read the required integer by parsing it from a string form like this:

```tut:book:silent
implicit val myIntReader = ConfigReader.fromString[MyInt](
  ConvertHelpers.catchReadError(s => new MyInt(s.toInt)))
```

The `fromString` factory method allows users to easily read data from string representations in the config.
`catchReadError` is a convenience function that catches exceptions thrown by the parsing code and transforms them into
[PureConfig errors](error-handling.html).

Finally, we could simply implement the `ConfigReader` interface by hand:

```tut:book:silent
implicit object MyIntReader extends ConfigReader[MyInt] {
  def from(cur: ConfigCursor) = cur.asString.right.map(s => new MyInt(s.toInt))
}
```

The inteface consists of a single `from` method that takes a `ConfigCursor` and returns an `Either` of a `MyInt` or a
list of errors. You can read more about cursors at [Config Cursors](config-cursors.html).

Using any of the approaches above would now make `loadConfig` work:

```tut:book
loadConfig[Conf](ConfigFactory.parseString("{ n: 1 }"))
```

The case above serves as an example for most simple types. While for those types it is straightforward to create a
`ConfigReader`, complex types that require access to an entire sub-tree of the configuration to be read can make
implementing an appropriate `ConfigReader` non-trivial. The [Complex Types](complex-types.html) section presents
different approaches for doing that, along with their advantages and disadvantages.
