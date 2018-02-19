
# Http4s module for PureConfig

Adds support for selected [Http4s](http://http4s.org/) classes to PureConfig.

## Add pureconfig-http4s to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-http4s" % "0.9.0"
```

## Example

To load an `Uri` into a configuration, create a class to hold it:

```scala
import org.http4s.Uri
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.modules.http4s._

case class MyConfig(uri: Uri)
```

We can read a `MyConfig` with the following code:

```scala
val conf = parseString("""{ uri: "http://http4s.org/" }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"uri":"http://http4s.org/"}))

loadConfig[MyConfig](conf)
// res0: Either[pureconfig.error.ConfigReaderFailures,MyConfig] = Right(MyConfig(http://http4s.org/))
```
