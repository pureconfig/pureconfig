# Javax module for PureConfig

Adds support for selected javax classes to PureConfig.



## Add pureconfig-javax to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.melrief" %% "pureconfig-javax" % "0.6.0"
```

## Example

```scala
// Load a KerberosPrincipal into a configuration

import javax.security.auth.kerberos.KerberosPrincipal
import pureconfig._
import pureconfig.module.javax._

case class MyConfig(principal: KerberosPrincipal)

import com.typesafe.config.ConfigFactory.parseString
val conf = parseString("""{ principal: "userid@tld.REALM" }""")
loadConfig[MyConfig](conf)
// Right(MyConfig(userid@tld.REALM)))
```


