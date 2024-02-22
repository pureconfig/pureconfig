# Pekko HTTP module for PureConfig

Adds support for [Pekko-Http](https://pekko.apache.org/docs/pekko-http/current/common/http-model.html)'s Uri class to PureConfig. PRs adding support
for other classes are welcome :)

## Add pureconfig-pekko-http to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-pekko-http" % "0.17.6"
```

## Example

To load an `Uri` into a configuration, create a class to hold it:

```scala
import org.apache.pekko.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.pekkohttp._

case class MyConfig(uri: Uri)
```

We can read a `MyConfig` with the following code:

```scala
val conf = ConfigFactory.parseString("""{ uri: "https://pekko.apache.org/docs/pekko-http/current/common/http-model.html" }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"uri":"https://pekko.apache.org/docs/pekko-http/current/common/http-model.html"}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: ConfigReader.Result[MyConfig] = Right(MyConfig(https://pekko.apache.org/docs/pekko-http/current/common/http-model.html))
```
