---
layout: docs
title: Quick Start
---

## {{page.title}}

To use PureConfig in an existing SBT project with Scala 2.10 or a later version, add the following dependency to your
`build.sbt`:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.7.2"
```

For Scala 2.10 you need also the Macro Paradise plugin:

```scala
libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.7.2",
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.patch)
)
```

For a full example of `build.sbt` you can have a look at this [build.sbt](https://github.com/pureconfig/pureconfig/blob/master/example/build.sbt).

Earlier versions of Scala had bugs which can cause subtle compile-time problems in PureConfig.
As a result we recommend only using the latest Scala versions within the minor series.

In your code, first import the library and define data types and a case class to hold the configuration:

```scala
import pureconfig.loadConfig

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
```

Second, define a configuration:

`application.json`
```json
{ 
  "boolean": true,
  "port": 8080, 
  "adt": { 
    "type": "adtb", 
    "b": 1 
  }, 
  "list": ["1", "20%"], 
  "map": { "key": "value" } 
}
```

Finally, load the configuration:

```scala
loadConfig[MyClass](conf)
// res3: Either[pureconfig.error.ConfigReaderFailures,MyClass] = Right(MyClass(true,Port(8080),AdtB(1),List(1.0, 0.2),Map(key -> value),None))
```

The various `loadConfig` methods defer to Typesafe Config's
[`ConfigFactory`](https://typesafehub.github.io/config/latest/api/com/typesafe/config/ConfigFactory.html) to
select where to load the config files from. Typesafe Config has [well-documented rules for configuration
loading](https://github.com/typesafehub/config#standard-behavior) which we'll not repeat. Please see Typesafe
Config's documentation for a full telling of the subtleties.

Alternatively, PureConfig also provides a `loadConfigFromFiles` method, which builds a configuration from
an explicit list of files. Files earlier in the list have greater precedence than later ones. Each file can
include a partial configuration as long as the whole list produces a complete configuration. For an example,
see the test of `loadConfigFromFiles` in
[`ApiSuite.scala`](https://github.com/pureconfig/pureconfig/blob/master/core/src/test/scala/pureconfig/ApiSuite.scala).

Because PureConfig uses Typesafe Config to load configuration, it supports reading files in [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), JSON, and Java `.properties` formats. HOCON is a delightful superset of both JSON and `.properties` that is highly recommended. As an added bonus it supports [advanced features](https://github.com/typesafehub/config/blob/master/README.md#features-of-hocon) like variable substitution and file sourcing.
