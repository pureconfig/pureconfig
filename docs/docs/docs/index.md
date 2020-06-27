---
layout: docs
title: Quick Start
---

## {{page.title}}

To use PureConfig in an existing SBT project with Scala 2.11 or a later version, add the following dependency to your
`build.sbt`:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.13.0"
```

For a full example of `build.sbt` you can have a look at this [build.sbt](https://github.com/pureconfig/pureconfig/blob/master/example/build.sbt).

Earlier versions of Scala had bugs which can cause subtle compile-time problems in PureConfig.
As a result we recommend only using the latest Scala versions within the minor series.

In your code, import `pureconfig.generic.auto` and define data types and a case class to hold the configuration:

```scala mdoc:reset-object
import pureconfig._
import pureconfig.generic.auto._

case class Port(number: Int) extends AnyVal

sealed trait AuthMethod
case class Login(username: String, password: String) extends AuthMethod
case class Token(token: String) extends AuthMethod
case class PrivateKey(pkFile: java.io.File) extends AuthMethod

case class ServiceConf(
  host: String,
  port: Port,
  useHttps: Boolean,
  authMethods: List[AuthMethod]
)
```

Second, create an `application.conf` file and add it as a resource of your application (with SBT, they are usually
placed in `src/main/resources`):

```scala mdoc:passthrough
println("```")
println(scala.io.Source.fromFile("docs/src/main/resources/application.conf").mkString.trim)
println("```")
```

Note the usage of different naming conventions for config keys and class fields, which you
[can customize later](overriding-behavior-for-case-classes.html).

Finally, load the configuration:

```scala mdoc
ConfigSource.default.load[ServiceConf]
```

`ConfigReader.Result[ServiceConf]` is just an alias for `Either[ConfigReaderFailures, ServiceConf]`, so you can handle
it just like you would handle an `Either` value.

`ConfigSource.default` defers to Typesafe Config's
[`ConfigFactory`](https://lightbend.github.io/config/latest/api/com/typesafe/config/ConfigFactory.html) to
select where to load the config files from. Typesafe Config has [well-documented rules for configuration
loading](https://github.com/lightbend/config#standard-behavior) which we'll not repeat. Please see Typesafe
Config's documentation for a full telling of the subtleties and see [Loading a Config](loading-a-config.html) for
alternative sources for configuration files.

Because PureConfig uses Typesafe Config to load configurations, it supports reading files in [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md#hocon-human-optimized-config-object-notation), JSON, and Java `.properties` formats. HOCON is a delightful superset of both JSON and `.properties` that is highly recommended. As an added bonus it supports [advanced features](https://github.com/lightbend/config/blob/master/README.md#features-of-hocon) like variable substitution and file sourcing.
