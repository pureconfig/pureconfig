# Cats module for PureConfig

Adds support for selected [cats](http://typelevel.org/cats/) classes to PureConfig.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/melrief/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.7.1"
```

## Example

To load a `NonEmptyList[Int]` into a configuration, we need a class to hold our configuration:

```tut:silent
import cats.data.{NonEmptyList, NonEmptyVector}
import com.typesafe.config.ConfigFactory.parseString
import pureconfig._
import pureconfig.module.cats._

case class MyConfig(numbers: NonEmptyList[Int])
```

We can read a `MyConfig` like:
```tut:book
val conf = parseString("""{ numbers: [1,2,3] }""")
loadConfig[MyConfig](conf)
```

You can also load `NonEmptyVector`. First, define a case class for the config:

```tut:silent
case class MyVecConfig(numbers: NonEmptyVector[Int])
```

then load the config:
```tut:book
loadConfig[MyVecConfig](conf)
```
