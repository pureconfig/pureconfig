# Javax module for PureConfig

Adds support for selected javax classes to PureConfig.

## Add pureconfig-javax to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.melrief" %% "pureconfig-javax" % "0.6.0"
```

## Example

Load a KerberosPrincipal into a configuration:

```tut:silent
import javax.security.auth.kerberos.KerberosPrincipal
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.javax._

case class MyConfig(principal: KerberosPrincipal)

val conf = parseString("""{ principal: "userid@tld.REALM" }""")
loadConfig[MyConfig](conf)
```


