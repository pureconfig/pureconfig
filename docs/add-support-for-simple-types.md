### Add support for simple types

Let's see how to support the type

```scala
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.syntax._

class MyInt(var value: Int) {
  override def toString: String = s"MyInt($value)"
}

case class Conf(n: MyInt)

val conf = parseString(s"""{ n: 1 }""")
```

First, define a `ConfigReader` instance in implicit scope:

```scala
import pureconfig._
import pureconfig.ConvertHelpers._

implicit val myIntReader = ConfigReader.fromString[MyInt](catchReadError(s => new MyInt(s.toInt)))
```

Then load the config:

```scala
loadConfig[Conf](conf)
// res4: Either[pureconfig.error.ConfigReaderFailures,Conf] = Right(Conf(MyInt(1)))
```

In some situations, you may want to write an object to a config. For unsupported types, you'll see the following error:

```scala
Conf(new MyInt(3)).toConfig
// <console>:30: error: could not find implicit value for parameter writer: pureconfig.ConfigWriter[Conf]
//        Conf(new MyInt(3)).toConfig
//                           ^
```

Just as with reading, you'll have to provide an instance of `ConfigWriter` for the type of the object you want to write:

```scala
implicit val myIntWriter = ConfigWriter.toString[MyInt](n => n.value.toString)
```

And then:

```scala
Conf(new MyInt(3)).toConfig
// res6: com.typesafe.config.ConfigValue = SimpleConfigObject({"n":"3"})
```

If you want to define both operations, the easiest way to add full support for a class is by creating a `ConfigConvert`:

```scala
implicit val myIntConvert = ConfigConvert.viaString[MyInt](
  catchReadError(s => new MyInt(s.toInt)),
  n => n.value.toString)
```

A `ConfigConvert` is both an instance of `ConfigReader` and an instance of `ConfigWriter`, so it can be used everywhere
one of them is required.
