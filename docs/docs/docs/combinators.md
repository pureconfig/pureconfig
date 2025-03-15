---
layout: docs
title: Combinators
---

## {{page.title}}

The combinators defined in
[`ConfigReader`](https://www.javadoc.io/page/com.github.pureconfig/pureconfig-core_2.13/latest/pureconfig/ConfigReader.html)
provide an easy way to create new `ConfigReader` instances by transforming existing ones. They are the simplest solution
for supporting new simple types and for slightly modifying existing implementations, since the amount of boilerplate
required is very small. This section contains some examples of combinators and shows how to work with them in
PureConfig.

The simplest combinator is `map`, which simply transforms the result of an existing reader:

```scala mdoc:silent
import pureconfig._
import pureconfig.generic.auto._

case class BytesConf(bytes: Vector[Byte])

// reads an array of bytes from a string
implicit val byteVectorReader: ConfigReader[Vector[Byte]] =
  ConfigReader[String].map(_.getBytes.toVector)
```

```scala mdoc
ConfigSource.string("""{ bytes = "Hello world" }""").load[BytesConf]
```

`emap` allows users to validate the inputs and provide detailed failures:

```scala mdoc:silent
import pureconfig.error._

case class Port(number: Int)
case class PortConf(port: Port)

// reads a TCP port, validating the number range
implicit val portReader = ConfigReader[Int].emap {
  case n if n >= 0 && n < 65536 => Right(Port(n))
  case n => Left(CannotConvert(n.toString, "Port", "Invalid port number"))
}
```

```scala mdoc
ConfigSource.string("{ port = 8080 }").load[PortConf]
ConfigSource.string("{ port = -1 }").load[PortConf]
```

`ensure` allows users to quickly fail a reader if a condition does not hold:

```scala mdoc:silent
import pureconfig.generic.semiauto._

case class Bounds(min: Int, max: Int)

implicit val boundsReader = deriveReader[Bounds]
  .ensure(b => b.max > b.min, _ => "Max must be bigger than Min")
```

```scala mdoc
ConfigSource.string("{ min = 1, max = 3 }").load[Bounds]
ConfigSource.string("{ min = 5, max = 3 }").load[Bounds]
```

`orElse` can be used to provide alternative ways to load a config:

```scala mdoc:silent
val csvIntListReader = ConfigReader[String].map(_.split(",").map(_.toInt).toList)
implicit val intListReader = ConfigReader[List[Int]].orElse(csvIntListReader)

case class IntListConf(list: List[Int])
```

```scala mdoc
ConfigSource.string("""{ list = [1,2,3] }""").load[IntListConf]
ConfigSource.string("""{ list = "4,5,6" }""").load[IntListConf]
```
