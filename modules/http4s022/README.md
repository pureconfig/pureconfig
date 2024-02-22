
# Http4s022 module for PureConfig

Adds support for [Http4s](http://http4s.org/)'s (v0.22.x) Uri class to PureConfig. PRs adding support
for other classes are welcome :)

**Note**: this is a compatibility module for those old projects only that are still stuck to Http4s v0.22.x (most likely due to dependencies on Cats Effect v2.x).
Newer projects should use a regular [PureConfig Http4s](https://github.com/pureconfig/pureconfig/tree/master/modules/http4s) module.

## Add pureconfig-http4s to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-http4s022" % "0.17.6"
```

## Example

To load an `Uri` into a configuration, create a class to hold it:

```scala
import org.http4s.Uri
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.http4s022._

case class MyConfig(uri: Uri)
```

We can read a `MyConfig` with the following code:

```scala
val conf = parseString("""{ uri: "http://http4s.org/" }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"uri":"http://http4s.org/"}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: ConfigReader.Result[MyConfig] = Right(MyConfig(http://http4s.org/))
```
