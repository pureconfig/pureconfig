# Doobie module for PureConfig

Adds support for [Doobie](https://tpolecat.github.io/doobie/)'s Hikari Config class to PureConfig. PRs adding support
for other classes are welcome :)

## Add pureconfig-doobie-hikari to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-doobie-hikari" % "0.17.2"
```

## Example

To load an `Config` into a configuration, create a class to hold it:

```scala
import com.typesafe.config.ConfigFactory.parseString
import doobie.hikari.Config
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.doobie.hikari._

case class MyConfig(config: Config)
```

We can read a `MyConfig` with the following code:

```scala
val conf = parseString("""{ config: { jdbc-url = "jdbc:postgresql://localhost:5432/postgres" } }""")

ConfigSource.fromConfig(conf).load[MyConfig]
```
