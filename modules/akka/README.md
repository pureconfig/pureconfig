# Akka module for PureConfig

Adds support for selected [Akka](http://akka.io/) classes to PureConfig.

## Add pureconfig-akka to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-akka" % "0.7.0"
```

## Example

To load a `Timeout` and an `ActorPath` into a configuration, we create a class to hold our configuration:

```scala
import akka.actor.ActorPath
import akka.util.Timeout
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.akka._

case class MyConfig(timeout: Timeout, actorPath: ActorPath)
```

We can read a `MyConfig` like:
```scala
val conf = parseString("""{ 
  timeout: 5 seconds, 
  actor-path:  "akka://my-sys/user/service-a/worker1"
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"actor-path":"akka://my-sys/user/service-a/worker1","timeout":"5 seconds"}))

loadConfig[MyConfig](conf)
// res1: Either[pureconfig.error.ConfigReaderFailures,MyConfig] = Right(MyConfig(Timeout(5 seconds),akka://my-sys/user/service-a/worker1))
```


