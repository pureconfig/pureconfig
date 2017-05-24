# Play Configuration module for PureConfig

Adds syntactic sugar to Play's 2.5's 
[Configuration](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.api.Configuration) class. 
This is similar to the syntactic sugar added to TypeSafe Config's `Config` class by `pureconfig.syntax._`

## Add pureconfig-play to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-play" % "0.7.1"
```

## Example

To load a configuration data into a case class using the Play Configuration class:


```scala
import play.api.Configuration
import pureconfig.module.play._

case class MyConfig(name: String)
```

We can read a `MyConfig` like:
```scala
val conf = Configuration.from(Map("name" -> "Susan"))
// conf: play.api.Configuration = Configuration(Config(SimpleConfigObject({"name":"Susan"})))

conf.to[MyConfig]
// res1: Either[pureconfig.error.ConfigReaderFailures,MyConfig] = Right(MyConfig(Susan))
```


