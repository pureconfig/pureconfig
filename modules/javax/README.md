# Javax module for PureConfig

Adds support for selected javax classes to PureConfig.

## Add pureconfig-javax to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-javax" % "0.12.1"
```

## Example

To load a KerberosPrincipal into a configuration, we need a class to hold our configuration:

```scala
import javax.security.auth.kerberos.KerberosPrincipal
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.javax._

case class MyConfig(principal: KerberosPrincipal)
```

We can read a `MyConfig` like:
```scala
val conf = parseString("""{ principal: "userid@tld.REALM" }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"principal":"userid@tld.REALM"}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: pureconfig.ConfigReader.Result[MyConfig] = Right(MyConfig(userid@tld.REALM))
```


