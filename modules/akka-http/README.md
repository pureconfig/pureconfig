
# Akka-Http module for PureConfig

Adds support for [Akka-Http](https://doc.akka.io/docs/akka-http/current/common/http-model.html#http-model)'s Uri class to PureConfig. PRs adding support
for other classes are welcome :)

## Add pureconfig-akka-http to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-akka-http" % "0.17.5"
```

## Example

To load an `Uri` into a configuration, create a class to hold it:

```scala
import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.akkahttp._

case class MyConfig(uri: Uri)
```

We can read a `MyConfig` with the following code:

```scala
val conf = ConfigFactory.parseString("""{ uri: "https://doc.akka.io/docs/akka-http/current/common/http-model.html#http-model" }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"uri":"https://doc.akka.io/docs/akka-http/current/common/http-model.html#http-model"}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: ConfigReader.Result[MyConfig] = Right(MyConfig(https://doc.akka.io/docs/akka-http/current/common/http-model.html#http-model))
```
