# Akka module for PureConfig

Adds support for selected [Akka](http://akka.io/) classes to PureConfig.

## Add pureconfig-akka to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-akka" % "0.7.0"
```

## Example

To load a `Timeout` into a configuration, we need a class to hold our configuration:

```tut:silent
import akka.util.Timeout
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.akka._

case class MyConfig(timeout: Timeout)
```

We can read a `MyConfig` like:
```tut:book
val conf = parseString("""{ timeout: 5 seconds }""")
loadConfig[MyConfig](conf)
```


