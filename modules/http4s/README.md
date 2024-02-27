
# Http4s module for PureConfig

Adds support for [Http4s](http://http4s.org/)'s `Uri` class to PureConfig. PRs adding support
for other classes are welcome :)

Support is also provided for some of the components of a `Uri`:

* `Uri.Scheme`
* `Uri.Path`
* `Uri.Host`
    * `Uri.Ipv4Address`
    * `Uri.Ipv6Address`

## Add pureconfig-http4s to your project

In addition to [core PureConfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-http4s" % "0.17.6"
```

## Example

To load a `Uri` or one of `Uri`'s components into a configuration, create a class to hold it:

```scala
import org.http4s.Uri
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.http4s._

case class MyConfig(
    uri: Uri,
    scheme: Uri.Scheme,
    host: Uri.Host,
    path: Uri.Path,
    ipAddress1: Uri.Ipv4Address,
    ipAddress2: Uri.Ipv6Address
)
```

We can read a `MyConfig` with the following code:

```scala
val conf = parseString("""{
    uri: "http://http4s.org/",
    scheme: "https",
    host: "www.foo.com",
    path: "relative/path/to/resource.txt",
    ip-address-1: "192.168.1.1",
    ip-address-2: "2001:db8:85a3:8d3:1319:8a2e:370:7344"
}""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"host":"www.foo.com","ip-address-1":"192.168.1.1","ip-address-2":"2001:db8:85a3:8d3:1319:8a2e:370:7344","path":"relative/path/to/resource.txt","scheme":"https","uri":"http://http4s.org/"}))

ConfigSource.fromConfig(conf).load[MyConfig]
// res0: ConfigReader.Result[MyConfig] = Right(MyConfig(http://http4s.org/,Scheme(https),www.foo.com,relative/path/to/resource.txt,Ipv4Address(192.168.1.1),[2001:db8:85a3:8d3:1319:8a2e:370:7344]))
```
