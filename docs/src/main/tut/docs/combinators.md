---
layout: docs
title: Combinators
---

## {{page.title}}

The combinators defined in `ConfigReader` are an easy way to create new `ConfigReader` instances by transforming
existing ones. They are the simplest solution for supporting new simple types and for slightly modifying existing
implementations, since the amount of boilerplate required is very small. This section will show some examples of
combinators and how to work with them in PureConfig.

The simplest combinator is `map`, which simply transforms the result of an existing reader:

```tut:silent
import com.typesafe.config.ConfigFactory
import pureconfig._

case class Conf(bytes: Vector[Byte])

// reads an array of bytes from a string
implicit val byteVectorReader = ConfigReader[String].map(_.getBytes.toVector)
```

```tut:book
loadConfig[Conf](ConfigFactory.parseString("""{ bytes = "Hello world" }"""))
```

`emap` allows you to validate the inputs and provide detailed failures:

```tut:silent
import pureconfig.error._

case class Port(number: Int)
case class Conf(port: Port)

// reads a TCP port, validating the number range
implicit val portReader = ConfigReader[Int].emap {
  case n if n >= 0 && n < 65536 => Right(Port(n))
  case n => Left(CannotConvert(n.toString, "Port", "Invalid port number"))
}
```

```tut:book
loadConfig[Conf](ConfigFactory.parseString("{ port = 8080 }"))
loadConfig[Conf](ConfigFactory.parseString("{ port = -1 }"))
```

`orElse` can be use to provide alternative ways to load a config:

```tut:silent
val defaultIntListReader = ConfigReader[List[Int]]
val csvIntListReader = ConfigReader[String].map(_.split(",").map(_.toInt).toList)

// scoping this in an object is important, as otherwise `defaultIntListReader`
// would be defined as our custom reader instead of the default one
object CustomReaders {
  // reads lists of integers either as config lists or as strings with
  // comma-separated numbers
  implicit val intListReader = defaultIntListReader.orElse(csvIntListReader)
}

import CustomReaders._

case class Conf(list: List[Int])
```

```tut:book
loadConfig[Conf](ConfigFactory.parseString("""{ list = [1,2,3] }"""))
loadConfig[Conf](ConfigFactory.parseString("""{ list = "4,5,6" }"""))
```

The full list of methods provided by `ConfigReader` can be seen
[here](https://static.javadoc.io/com.github.pureconfig/pureconfig_2.12/0.8.0/pureconfig/ConfigReader.html).
