# Akka module for PureConfig

Adds support for selected [Akka](http://akka.io/) classes to PureConfig.

## Add pureconfig-akka to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-akka" % "0.7.2"
```

## Example

To load a `Timeout` and an `ActorPath` into a configuration, we create a class to hold our configuration:

```tut:silent
import akka.actor.ActorPath
import akka.util.Timeout
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.akka._

case class MyConfig(timeout: Timeout, actorPath: ActorPath)
```

We can read a `MyConfig` like:
```tut:book
val conf = parseString("""{ 
  timeout: 5 seconds, 
  actor-path:  "akka://my-sys/user/service-a/worker1"
}""")
loadConfig[MyConfig](conf)
```


