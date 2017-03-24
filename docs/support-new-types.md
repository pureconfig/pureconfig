## Support new types

Not all types are supported automatically by PureConfig. For instance, classes
that are not case classes are not supported out-of-the-box:

```scala
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._

class MyInt(var value: Int) {
  override def toString: String = s"MyInt($value)"
}

case class Conf(n: MyInt)

val conf = parseString(s"""{ n: 1 }""")
```

This won't compile because there's no `ConfigConvert` instance for `MyInt`:
```scala
loadConfig[Conf](conf)
// <console>:20: error: could not find implicit value for parameter conv: pureconfig.ConfigReader[Conf]
//        loadConfig[Conf](conf)
//                        ^
```

PureConfig can be extended to support those types. To do so, an instance for the
`ConfigConvert` type class must be provided.

First, define a `ConfigConvert` instance in implicit scope:

```scala
import pureconfig.ConfigConvert._

implicit val myIntConvert = ConfigConvert.viaString[MyInt](catchReadError(s => new MyInt(s.toInt)), n => n.value.toString)
```

Then load the config:
```scala
loadConfig[Conf](conf)
// res5: Either[pureconfig.error.ConfigReaderFailures,Conf] = Right(Conf(MyInt(1)))
```
