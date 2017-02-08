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

loadConfig[Conf](conf)
// doesn't compile - could not find implicit value for parameter conv: pureconfig.ConfigConvert[Conf]
```

PureConfig can be extended to support those types. To do so, an instance for the
`ConfigConvert` type class must be provided implicitly, like:

```scala
import scala.util.Try

implicit val myIntConvert = ConfigConvert.stringConvert[MyInt](s => Try(new MyInt(s.toInt)), n => n.value.toString)

loadConfig[Conf](conf)
// returns Success(Conf(new MyInt(1)))
```
