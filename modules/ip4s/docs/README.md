
# Ip4s module for PureConfig

Adds support for [Ip4s](https://github.com/Comcast/ip4s)'s Hostname and Port class to PureConfig. PRs adding support
for other classes are welcome :)

## Add pureconfig-ip4s to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-ip4s" % "@VERSION@"
```

## Example

To load an `Hostname` or a `Port` into a configuration, create a class to hold it:

```scala mdoc:silent
import com.comcast.ip4s.{Hostname, Port}
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.ip4s._

case class MyConfig(hostname: Hostname, port: Port)
```

We can read a `MyConfig` with the following code:

```scala mdoc:silent
val source = ConfigSource.string(
  """{ 
    |hostname: "0.0.0.0" 
    |port: 8080 
    |}""".stripMargin)
    
source.load[MyConfig]
```
