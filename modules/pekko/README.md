# Pekko module for PureConfig

Adds support for selected [Pekko](https://pekko.apache.org/) classes to PureConfig.

## Add pureconfig-pekko to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-pekko" % "0.17.10"
```

## Example

To load a `Timeout` and an `ActorPath` into a configuration, we create a class to hold our configuration:

```scala
import org.apache.pekko.actor.ActorPath
import org.apache.pekko.util.Timeout
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.pekko._

case class MyConfig(timeout: Timeout, actorPath: ActorPath)
```

We can read a `MyConfig` like:
```scala
val conf = parseString("""{
  timeout: 5 seconds,
  actor-path:  "pekko://my-sys/user/service-a/worker1"
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"actor-path":"pekko://my-sys/user/service-a/worker1","timeout":"5 seconds"}))
ConfigSource.fromConfig(conf).load[MyConfig]
// res0: ConfigReader.Result[MyConfig] = Right(
//   value = MyConfig(
//     timeout = Timeout(duration = 5 seconds),
//     actorPath = pekko://my-sys/user/service-a/worker1
//   )
// )
```
