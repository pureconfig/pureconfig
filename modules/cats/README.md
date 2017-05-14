# Cats module for PureConfig

Adds support for selected cats classes to PureConfig.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.7.1"
```

## Example

To load a `NonEmptyList[Int]` into a configuration, we need a class to hold our configuration:

```scala
import cats.data.{NonEmptyList, NonEmptyVector}
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.cats._

case class MyConfig(numbers: NonEmptyList[Int])
```

We can read a `MyConfig` like:
```scala
val conf = parseString("""{ numbers: [1,2,3] }""")
// conf: com.typesafe.config.Config = Config(SimpleConfigObject({"numbers":[1,2,3]}))

loadConfig[MyConfig](conf)
// res1: Either[pureconfig.error.ConfigReaderFailures,MyConfig] = Right(MyConfig(NonEmptyList(1, 2, 3)))
```

You can also load `NonEmptyVector`. First, define a case class for the config:

```scala
case class MyVecConfig(numbers: NonEmptyVector[Int])
```

then load the config:
```scala
loadConfig[MyVecConfig](conf)
// res2: Either[pureconfig.error.ConfigReaderFailures,MyVecConfig] = Right(MyVecConfig(NonEmptyVector(1, 2, 3)))
```
