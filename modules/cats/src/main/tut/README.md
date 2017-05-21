# Cats module for PureConfig

Adds support for selected [cats](http://typelevel.org/cats/) data structures to PureConfig and provides instances of
`cats` type classes for `ConfigReader`,  `ConfigWriter` and `ConfigConvert`.

## Add pureconfig-cats to your project

In addition to [core pureconfig](https://github.com/pureconfig/pureconfig), you'll need:

```scala
libraryDependencies += "com.github.pureconfig" %% "pureconfig-cats" % "0.7.1"
```

## Example

### Reading cats data structures from a config

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

### Using cats type class instances for readers and writers

In order to to put in scope the `cats` type classes for our readers and writers and extend the latter with the extra
operations provided by `cats`, we need some extra imports:

```tut:silent
import cats._
import cats.implicits._
import pureconfig.module.cats.instances._
import pureconfig.syntax._
```

We are now ready to use the new syntax:

```tut:silent
// a reader that returns a constant value
val constIntReader = Monad[ConfigReader].pure(42)

// a Int reader that returns -1 if an error occurs
val safeIntReader = ConfigReader[Int].handleError(_ => -1)

// a writer for `Some[A]` (note that the `ConfigWriter` trait is invariant)
implicit def someWriter[A: ConfigWriter] = ConfigWriter[Option[A]].narrow[Some[A]]
```

And we can finally put them to use:

```tut:book
constIntReader.from(conf.root())

safeIntReader.from(conf.root())

Some("abc").toConfig
```
