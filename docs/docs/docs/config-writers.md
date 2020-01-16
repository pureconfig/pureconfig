---
layout: docs
title: Config Writers
---

## {{page.title}}

The main use case for PureConfig, as described in the homepage, is to load configuration files to Scala classes in a
typesafe and boilerplate-free way. However, there are situations where users may have the need to do the inverse
operation: to write a config file from a Scala data structure. An example would be to save a config after it is changed
in-app.

Just as PureConfig provides a `ConfigReader` interface for reading configurations, it also provides a `ConfigWriter` for
writing configs.

All types mentioned at [Built-in Supported Types](built-in-supported-types.md) are supported both in reading and in
writing operations:

```scala mdoc:silent:reset-object
import pureconfig._
import pureconfig.generic.auto._

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
  
val confObj = MyClass(true, Port(8080), AdtB(1), List(1.0, 0.2), Map("key" -> "value"), None)
```

```scala mdoc
ConfigWriter[MyClass].to(confObj)
```

The mechanisms with which PureConfig finds out how to write a type to a config are the same as ones used with
`ConfigReader`. Therefore, you can use most tutorials and tips at [Supporting New Types](supporting-new-types.md)
and [Overriding Behavior for Types](overriding-behavior-for-types.md) for creating `ConfigWriter` instances, too.
`ConfigWriter` also has useful combinators and factory methods to simplify new implementations:

```scala mdoc:silent
class MyInt(value: Int) {
  def getValue: Int = value
  override def toString: String = s"MyInt($value)"
}

implicit val myIntWriter = ConfigWriter[Int].contramap[MyInt](_.getValue)
```

```scala mdoc
ConfigWriter[MyInt].to(new MyInt(1))
```

Finally, if you need both the reading and the writing part for a custom type, you can implement a `ConfigConvert`:

```scala mdoc:silent
implicit val myIntConvert = ConfigConvert[Int].xmap[MyInt](new MyInt(_), _.getValue)
```

```scala mdoc
val conf = ConfigWriter[MyInt].to(new MyInt(1))
ConfigReader[MyInt].from(conf)
```

A `ConfigConvert` implements both the `ConfigReader` and `ConfigWriter` interfaces and can be used everywhere one of
them is needed.
