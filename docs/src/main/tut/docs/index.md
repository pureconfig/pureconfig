---
layout: docs
title: Quick Start
---

## {{page.title}}

To use PureConfig in an existing SBT project with Scala 2.11 or a later version, add the following dependency to your
`build.sbt`:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.12.0"
```

For a full example of `build.sbt` you can have a look at this [build.sbt](https://github.com/pureconfig/pureconfig/blob/master/example/build.sbt).

Earlier versions of Scala had bugs which can cause subtle compile-time problems in PureConfig.
As a result we recommend only using the latest Scala versions within the minor series.

In your code, import `pureconfig.generic.auto` and define data types and a case class to hold the configuration:

```tut:silent
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
```

Second, define an `application.conf` file like
[this](https://github.com/pureconfig/pureconfig/blob/master/docs/src/main/resources/application.conf) and add it as a
resource file of your application (with SBT, they are usually placed in `src/main/resources`).

Finally, load the configuration:

```tut:book
ConfigSource.default.load[MyClass]
```

`ConfigReader.Result[MyClass]` is just an alias for `Either[ConfigReaderFailures, MyClass]`, so you can handle it just like you
would handle an `Either` value.

`ConfigSource.default` defers to Typesafe Config's
[`ConfigFactory`](https://lightbend.github.io/config/latest/api/com/typesafe/config/ConfigFactory.html) to
select where to load the config files from. Typesafe Config has [well-documented rules for configuration
loading](https://github.com/lightbend/config#standard-behavior) which we'll not repeat. Please see Typesafe
Config's documentation for a full telling of the subtleties and see [Loading a Config](loading-a-config.html) for
alternative sources for configuration files.

Because PureConfig uses Typesafe Config to load configurations, it supports reading files in [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), JSON, and Java `.properties` formats. HOCON is a delightful superset of both JSON and `.properties` that is highly recommended. As an added bonus it supports [advanced features](https://github.com/lightbend/config/blob/master/README.md#features-of-hocon) like variable substitution and file sourcing.
