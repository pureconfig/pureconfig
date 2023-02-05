
# Ip4s module for PureConfig

Adds support for [Ip4s](https://github.com/Comcast/ip4s)'s models to PureConfig.
Currently the following models are supported:
  - `Host`:
    - `Hostname`
    - `IpAddress`:
      - `Ipv4Address`
      - `Ipv6Address`
    - `IDN`
  - `Port`

PRs adding support for other classes are welcome :)

## Add pureconfig-ip4s to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-ip4s" % "0.17.2"
```

## Example

To load an `Host` or a `Port` into a configuration, create a class to hold it:

```scala
import com.comcast.ip4s._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.ip4s._

case class MyConfig(host: Host, port: Port)
```

We can read a `MyConfig` with the following code:

```scala
val source = ConfigSource.string(
  """{ 
    |host: "0.0.0.0" 
    |port: 8080 
    |}""".stripMargin)
    
source.load[MyConfig]
```

Note that in the example above, `host` will be loaded as `Ipv4Address` type,
whereas `host: "pureconfig.github.io"` will become `Hostname` instead.
